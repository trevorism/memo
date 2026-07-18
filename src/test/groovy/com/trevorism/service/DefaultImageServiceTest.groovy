package com.trevorism.service

import com.trevorism.model.Image
import org.junit.jupiter.api.Test

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Covers the repository-backed image operations. Bucket I/O is exercised through a fake
 * that overrides the fetch/store/delete seams, so no live MultipartHttpClient is needed.
 */
class DefaultImageServiceTest {

    @Test
    void testGetImage() {
        def service = buildService([new Image(id: "i1", username: "alice")])
        assert service.getImage("i1").username == "alice"
        assert service.getImage("missing") == null
    }

    @Test
    void testListImagesUploadedByOthers() {
        def service = buildService([
                new Image(id: "i1", username: "alice"),
                new Image(id: "i2", username: "bob"),
                new Image(id: "i3", username: "carol")
        ])
        def others = service.listImagesUploadedByOthers("alice")
        assert others*.username.toSet() == ["bob", "carol"].toSet()
        assert others.every { it.username != "alice" }
    }

    @Test
    void testListImagesUploadedBy() {
        def service = buildService([
                new Image(id: "i1", username: "alice"),
                new Image(id: "i2", username: "bob"),
                new Image(id: "i3", username: "alice")
        ])
        assert service.listImagesUploadedBy("alice")*.id.toSet() == ["i1", "i3"].toSet()
    }

    @Test
    void testUpdateCaptionMissingReturnsNull() {
        assert buildService().updateCaption("missing", "hi") == null
    }

    @Test
    void testUpdateCaptionTrims() {
        def service = buildService([new Image(id: "i1", username: "alice")])
        Image updated = service.updateCaption("i1", "  a caption  ")
        assert updated.caption == "a caption"
    }

    @Test
    void testDeleteImageMissingReturnsFalse() {
        assert !buildService().deleteImage("missing")
    }

    @Test
    void testDeleteImageWithoutBucketPath() {
        // No bucketPath => no bucket call => hermetic; just removes the record.
        def service = buildService([new Image(id: "i1", username: "alice", bucketPath: null)])
        assert service.deleteImage("i1")
        assert service.getImage("i1") == null
    }

    @Test
    void testCreateImageStoresOriginalAndThumbnail() {
        def service = buildFakeBucketService()

        Image image = service.createImage(new ByteArrayInputStream(jpegBytes(800, 600)), "photo.jpg", "Alice", "  a caption  ")

        assert image.bucketPath.startsWith("memowand/alice/")
        assert image.bucketPath.endsWith("-photo.jpg")
        assert image.thumbnailPath.startsWith("memowand/alice/thumb/")
        assert image.thumbnailPath.endsWith(".jpg")
        assert image.caption == "a caption"
        assert service.stored.size() == 2

        BufferedImage thumb = ImageIO.read(new ByteArrayInputStream(service.stored[image.thumbnailPath]))
        assert Math.max(thumb.width, thumb.height) == ThumbnailGenerator.DEFAULT_MAX_EDGE
    }

    @Test
    void testCreateImageSkipsThumbnailWhenSourceUnreadable() {
        def service = buildFakeBucketService()

        Image image = service.createImage(new ByteArrayInputStream("not an image".bytes), "notes.txt", "Alice", null)

        assert image.bucketPath
        assert image.thumbnailPath == null
        assert service.stored.size() == 1
    }

    @Test
    void testEnsureThumbnailGeneratesAndPersists() {
        def service = buildFakeBucketService([new Image(id: "i1", username: "Alice", bucketPath: "memowand/alice/orig.jpg")])
        service.cannedBytes = jpegBytes(400, 400)

        Image updated = service.ensureThumbnail(service.getImage("i1"))

        assert updated.thumbnailPath?.startsWith("memowand/alice/thumb/")
        assert service.getImage("i1").thumbnailPath == updated.thumbnailPath
        assert service.stored.containsKey(updated.thumbnailPath)
    }

    @Test
    void testEnsureThumbnailIsNoOpWhenAlreadyPresent() {
        def service = buildFakeBucketService([new Image(id: "i1", username: "alice", bucketPath: "orig.jpg", thumbnailPath: "thumb.jpg")])

        Image result = service.ensureThumbnail(service.getImage("i1"))

        assert result.thumbnailPath == "thumb.jpg"
        assert service.stored.isEmpty()
    }

    @Test
    void testEnsureThumbnailLeavesUnsetWhenSourceUnsupported() {
        def service = buildFakeBucketService([new Image(id: "i1", username: "alice", bucketPath: "orig.webp")])
        service.cannedBytes = "unsupported".bytes

        Image result = service.ensureThumbnail(service.getImage("i1"))

        assert result.thumbnailPath == null
        assert service.stored.isEmpty()
    }

    @Test
    void testGetThumbnailData() {
        def service = buildFakeBucketService()
        service.cannedBytes = "thumb-bytes".bytes

        assert service.getThumbnailData(new Image(thumbnailPath: null)) == null
        assert service.getThumbnailData(new Image(thumbnailPath: "thumb.jpg")).toList() == "thumb-bytes".bytes.toList()
    }

    @Test
    void testClearThumbnailDeletesObjectAndUnsetsPath() {
        def service = buildFakeBucketService([new Image(id: "i1", username: "alice", bucketPath: "orig.jpg", thumbnailPath: "thumb.jpg")])

        Image result = service.clearThumbnail("i1")

        assert result.thumbnailPath == null
        assert service.getImage("i1").thumbnailPath == null
        assert service.deleted == ["https://bucket.data.trevorism.com/object/thumb.jpg"]
    }

    @Test
    void testClearThumbnailMissingReturnsNull() {
        assert buildFakeBucketService().clearThumbnail("missing") == null
    }

    @Test
    void testDeleteImageRemovesOriginalAndThumbnailObjects() {
        def service = buildFakeBucketService([new Image(id: "i1", username: "alice", bucketPath: "orig.jpg", thumbnailPath: "thumb.jpg")])

        assert service.deleteImage("i1")
        assert service.deleted.size() == 2
        assert service.getImage("i1") == null
    }

    private static DefaultImageService buildService(List<Image> seed = []) {
        DefaultImageService service = new DefaultImageService(null)
        service.repository = new InMemoryRepository<Image>(seed)
        return service
    }

    private static FakeBucketImageService buildFakeBucketService(List<Image> seed = []) {
        FakeBucketImageService service = new FakeBucketImageService()
        // repository is a private field on the superclass; set it directly for the subclass instance.
        def field = DefaultImageService.getDeclaredField("repository")
        field.setAccessible(true)
        field.set(service, new InMemoryRepository<Image>(seed))
        return service
    }

    private static byte[] jpegBytes(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        def graphics = image.createGraphics()
        graphics.fillRect(0, 0, width, height)
        graphics.dispose()
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        ImageIO.write(image, "jpg", out)
        return out.toByteArray()
    }

    /** Records bucket writes/deletes and serves canned bytes instead of hitting the network. */
    private static class FakeBucketImageService extends DefaultImageService {
        Map<String, byte[]> stored = [:]
        List<String> deleted = []
        byte[] cannedBytes

        FakeBucketImageService() { super(null) }

        @Override
        protected byte[] fetchBytes(String url) { return cannedBytes }

        @Override
        protected String storeBucketObject(String objectPath, byte[] content, String filename) {
            stored.put(objectPath, content)
            return objectPath
        }

        @Override
        protected void deleteBucketObject(String url) { deleted.add(url) }
    }
}
