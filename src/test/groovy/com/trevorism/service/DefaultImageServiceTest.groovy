package com.trevorism.service

import com.trevorism.model.Image
import org.junit.jupiter.api.Test

/**
 * Covers the repository-backed image operations. Methods that hit the storage bucket
 * (createImage/getImageData) are excluded since they require a live MultipartHttpClient.
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

    private static DefaultImageService buildService(List<Image> seed = []) {
        DefaultImageService service = new DefaultImageService(null)
        service.repository = new InMemoryRepository<Image>(seed)
        return service
    }
}
