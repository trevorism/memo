package com.trevorism.service

import com.trevorism.model.Image
import io.micronaut.http.multipart.CompletedFileUpload

interface ImageService {

    List<Image> listImages()

    List<Image> listImagesUploadedBy(String uploadedBy)

    List<Image> listImagesUploadedByOthers(String uploadedBy)

    Image getImage(String id)

    Image createImage(CompletedFileUpload file, String uploadedBy, String caption)

    Image createImage(InputStream inputStream, String filename, String uploadedBy, String caption)

    Image updateCaption(String id, String caption)

    byte[] getImageData(Image image)

    boolean deleteImage(String id)
}

