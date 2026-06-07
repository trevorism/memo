package com.trevorism.service

import com.trevorism.model.Image
import io.micronaut.http.multipart.CompletedFileUpload

interface ImageService {

    List<Image> listImages()

    List<Image> listImagesUploadedBy(String uploadedBy)

    List<Image> listImagesUploadedByOthers(String uploadedBy)

    Image getImage(String id)

    Image createImage(CompletedFileUpload file, String uploadedBy)

    byte[] getImageData(Image image)

    boolean deleteImage(String id)
}

