package com.trevorism.controller

import com.trevorism.model.Image
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.multipart.CompletedFileUpload
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/image")
class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class.name)

    @Tag(name = "Image Operations")
    @Operation(summary = "Get all images in an album")
    @Get(value = "/album/{albumId}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    List<Image> listByAlbum(String albumId) {
        // TODO: Implement with datastore client
        return []
    }

    @Tag(name = "Image Operations")
    @Operation(summary = "Get an image by id")
    @Get(value = "/{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    Image getById(String id) {
        // TODO: Implement with datastore client
        return null
    }

    @Tag(name = "Image Operations")
    @Operation(summary = "Upload an image to an album **Secure")
    @Post(value = "/album/{albumId}", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    Image upload(String albumId, CompletedFileUpload file) {
        // TODO: Upload to bucket service and create datastore entry
        return new Image(albumId: albumId, fileName: file.filename)
    }

    @Tag(name = "Image Operations")
    @Operation(summary = "Get the raw image data for download/viewing")
    @Get(value = "/{id}/raw")
    @Secure(value = Roles.USER, allowInternal = true)
    byte[] getRaw(String id) {
        // TODO: Fetch from bucket service
        return [] as byte[]
    }

    @Tag(name = "Image Operations")
    @Operation(summary = "Delete an image **Secure")
    @Delete(value = "/{id}", produces = MediaType.TEXT_PLAIN)
    @Secure(value = Roles.USER, allowInternal = true)
    String delete(String id) {
        // TODO: Delete from bucket and datastore
        return "deleted"
    }
}
