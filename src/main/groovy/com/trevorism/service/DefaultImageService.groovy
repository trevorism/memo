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
    Image createImage(CompletedFileUpload file, String username) {
        String bucketPath = storeImage(file, username)
        Image image = new Image()
        image.username = username
        image.bucketPath = bucketPath
        return repository.create(image)
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

    private String storeImage(CompletedFileUpload file, String username) {
        String guid = UUID.randomUUID().toString()
        String url = "https://bucket.data.trevorism.com/object/memowand/${username}/${guid}-${file.metadata.fileName()}"
        byte[] dataToSend = file.getBytes()
        String result = new MultipartHttpClient(httpClient.obtainTokenStrategy).post(url, dataToSend, file.filename)
        return result
    }

    private static String buildBucketObjectUrl(String bucketPath) {
        if (bucketPath.startsWith("http")) {
            return bucketPath
        }
        String cleaned = bucketPath.startsWith("/") ? bucketPath.substring(1) : bucketPath
        return "https://bucket.data.trevorism.com/object/${cleaned}"
    }

}

