package com.trevorism.service

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.data.model.filtering.FilterConstants
import com.trevorism.data.model.filtering.SimpleFilter
import com.trevorism.data.model.sorting.Sort
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Image
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.runtime.http.scope.RequestScope
import jakarta.inject.Named
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@RequestScope
class DefaultImageService implements ImageService {

    private static final Logger log = LoggerFactory.getLogger(DefaultImageService)

    private Repository<Image> repository
    private final SecureHttpClient httpClient
    private final ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator()
    private static final Closure sortByCreatedDateDesc = { a, b ->
        Date bDate = b?.createdDate ?: new Date(0)
        Date aDate = a?.createdDate ?: new Date(0)
        bDate <=> aDate
    }

    DefaultImageService(@Named("passThruSecureHttpClient") SecureHttpClient httpClient) {
        repository = new FastDatastoreRepository<>(Image, httpClient)
        this.httpClient = httpClient;
    }

    @Override
    List<Image> listImages() {
        repository.sort(new Sort("createdDate",true))
    }

    @Override
    List<Image> listImagesUploadedBy(String username) {
        repository.filter(new SimpleFilter("username", FilterConstants.OPERATOR_EQUAL, username)).sort sortByCreatedDateDesc
    }

    @Override
    List<Image> listImagesUploadedByOthers(String username) {
        repository.list().findAll { it.username != username }.sort sortByCreatedDateDesc
    }

    @Override
    Image getImage(String id) {
        repository.get(id)
    }

    @Override
    Image createImage(CompletedFileUpload file, String username, String caption) {
        return file.getInputStream().withCloseable { InputStream inputStream ->
            createImage(inputStream, file.filename, username, caption)
        }
    }

    @Override
    Image createImage(InputStream inputStream, String filename, String username, String caption) {
        byte[] bytes = inputStream.bytes
        String safeName = sanitizeFilename(filename)
        String safeUser = normalizeUsername(username)
        String guid = UUID.randomUUID().toString()

        Image image = new Image()
        image.username = username
        image.bucketPath = storeBucketObject("memowand/${safeUser}/${guid}-${safeName}", bytes, safeName)
        image.caption = caption?.trim()

        // Best-effort thumbnail; a null result (e.g. unsupported source) just leaves it unset.
        byte[] thumbnail = thumbnailGenerator.generateJpeg(bytes)
        if (thumbnail) {
            String thumbName = "${guid}.jpg"
            image.thumbnailPath = storeBucketObject("memowand/${safeUser}/thumb/${thumbName}", thumbnail, thumbName)
        }

        return repository.create(image)
    }

    @Override
    Image updateCaption(String id, String caption) {
        Image image = repository.get(id)
        if (!image) {
            return null
        }
        image.caption = caption?.trim()
        return repository.update(id, image)
    }

    @Override
    byte[] getImageData(Image image) {
        if (!image?.bucketPath) {
            return null
        }
        return fetchBytes(buildBucketObjectUrl(image.bucketPath))
    }

    @Override
    byte[] getThumbnailData(Image image) {
        if (!image?.thumbnailPath) {
            return null
        }
        return fetchBytes(buildBucketObjectUrl(image.thumbnailPath))
    }

    @Override
    Image ensureThumbnail(Image image) {
        if (!image || image.thumbnailPath || !image.bucketPath) {
            return image
        }

        byte[] thumbnail = thumbnailGenerator.generateJpeg(getImageData(image))
        if (!thumbnail) {
            // Unsupported source (e.g. WebP); leave unset so callers fall back to the original.
            return image
        }

        String safeUser = normalizeUsername(image.username)
        String thumbName = "${UUID.randomUUID()}.jpg"
        image.thumbnailPath = storeBucketObject("memowand/${safeUser}/thumb/${thumbName}", thumbnail, thumbName)
        return repository.update(image.id, image)
    }

    @Override
    Image clearThumbnail(String id) {
        Image image = repository.get(id)
        if (!image) {
            return null
        }
        if (image.thumbnailPath) {
            try {
                deleteBucketObject(buildBucketObjectUrl(image.thumbnailPath))
            } catch (Exception e) {
                log.warn("Unable to delete stale thumbnail object for image {}", id, e)
            }
            image.thumbnailPath = null
            repository.update(id, image)
        }
        return image
    }

    @Override
    boolean deleteImage(String id) {
        Image image = repository.get(id)
        if (!image) {
            return false
        }

        [image.bucketPath, image.thumbnailPath].findAll().each { String path ->
            try {
                deleteBucketObject(buildBucketObjectUrl(path))
            } catch (Exception e) {
                log.warn("Unable to delete bucket object {} for image {}", path, id, e)
            }
        }

        repository.delete(id)
        return true
    }

    // --- Bucket I/O seams (overridden in unit tests to stay hermetic) ---

    protected byte[] fetchBytes(String url) {
        return new MultipartHttpClient(httpClient.obtainTokenStrategy).getBytes(url)
    }

    protected String storeBucketObject(String objectPath, byte[] content, String filename) {
        String url = "https://bucket.data.trevorism.com/object/${objectPath}"
        return new MultipartHttpClient(httpClient.obtainTokenStrategy).post(url, content, filename)
    }

    protected void deleteBucketObject(String url) {
        new MultipartHttpClient(httpClient.obtainTokenStrategy).delete(url)
    }

    private static String sanitizeFilename(String filename) {
        String base = filename ?: 'image'
        int slash = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'))
        if (slash >= 0) {
            base = base.substring(slash + 1)
        }
        // Bucket object paths must be URL-safe; replace spaces and other characters.
        base = base.replaceAll(/[^A-Za-z0-9._-]/, '_')
        return base ?: 'image'
    }

    private static String normalizeUsername(String username) {
        // Used only for the URL-safe bucket path segment; the stored username is unchanged.
        String base = (username ?: 'unknown').trim().toLowerCase()
        base = base.replaceAll(/[^a-z0-9._-]/, '_')
        return base ?: 'unknown'
    }

    private static String buildBucketObjectUrl(String bucketPath) {
        if (bucketPath.startsWith("http")) {
            return bucketPath
        }
        String cleaned = bucketPath.startsWith("/") ? bucketPath.substring(1) : bucketPath
        return "https://bucket.data.trevorism.com/object/${cleaned}"
    }

}

