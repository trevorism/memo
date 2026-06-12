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
        String bucketPath = storeImage(inputStream, filename, username)
        Image image = new Image()
        image.username = username
        image.bucketPath = bucketPath
        image.caption = caption?.trim()
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
        String url = buildBucketObjectUrl(image.bucketPath)
        return new MultipartHttpClient(httpClient.obtainTokenStrategy).getBytes(url)
    }

    @Override
    boolean deleteImage(String id) {
        Image image = repository.get(id)
        if (!image) {
            return false
        }

        if (image.bucketPath) {
            try {
                String url = buildBucketObjectUrl(image.bucketPath)
                new MultipartHttpClient(httpClient.obtainTokenStrategy).delete(url)
            } catch (Exception e) {
                log.warn("Unable to delete bucket object for image {}", id, e)
            }
        }

        repository.delete(id)
        return true
    }

    private String storeImage(InputStream inputStream, String filename, String username) {
        String guid = UUID.randomUUID().toString()
        String safeName = sanitizeFilename(filename)
        String safeUser = normalizeUsername(username)
        String url = "https://bucket.data.trevorism.com/object/memowand/${safeUser}/${guid}-${safeName}"
        return new MultipartHttpClient(httpClient.obtainTokenStrategy).post(url, inputStream, safeName)
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

