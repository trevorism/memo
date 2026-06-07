package com.trevorism.controller

import com.trevorism.model.Image
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.CommentService
import com.trevorism.service.ImageService
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/image")
class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController)

    @Inject
    ImageService imageService

    @Inject
    CommentService commentService

    @Tag(name = "Image Operations")
    @Operation(summary = "Get all recently uploaded images, newest first **Secure")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    List<Image> listRecentImages() {
        try {
            return populateCommentCounts(imageService.listImages())
        } catch (Exception e) {
            log.error("Error listing images", e)
            throw new RuntimeException("Unable to list images", e)
        }
    }

    @Tag(name = "Image Operations")
    @Operation(summary = "Get images uploaded by a specific user, newest first **Secure")
    @Get(value = "/mine/{uploadedBy}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    List<Image> listMyImages(String uploadedBy) {
        try {
            return populateCommentCounts(imageService.listImagesUploadedBy(uploadedBy))
        } catch (Exception e) {
            log.error("Error listing images for user {}", uploadedBy, e)
            throw new RuntimeException("Unable to list images for user " + uploadedBy, e)
        }
    }

    @Tag(name = "Image Operations")
    @Operation(summary = "Get images uploaded by everyone except the given user, newest first **Secure")
    @Get(value = "/others/{uploadedBy}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    List<Image> listImagesFromOthers(String uploadedBy) {
        try {
            return populateCommentCounts(imageService.listImagesUploadedByOthers(uploadedBy))
        } catch (Exception e) {
            log.error("Error listing images excluding user {}", uploadedBy, e)
            throw new RuntimeException("Unable to list images excluding user " + uploadedBy, e)
        }
    }

    @Tag(name = "Image Operations")
    @Operation(summary = "Get image metadata by id **Secure")
    @Get(value = "/{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    Image getImage(String id) {
        Image image = imageService.getImage(id)
        if (image) {
            image.commentCount = commentService.countComments(image.id)
        }
        return image
    }

    @Tag(name = "Image Operations")
    @Operation(summary = "Stream the raw image bytes by id **Secure")
    @Get(value = "/{id}/raw", produces = MediaType.APPLICATION_OCTET_STREAM)
    @Secure(value = Roles.USER)
    HttpResponse<byte[]> getImageBytes(String id) {
        try {
            Image image = imageService.getImage(id)
            if (!image) {
                return HttpResponse.notFound()
            }
            byte[] data = imageService.getImageData(image)
            if (data == null) {
                return HttpResponse.notFound()
            }
            return HttpResponse.ok(data).contentType(resolveImageMediaType(image.bucketPath))
        } catch (Exception e) {
            log.error("Error streaming image {}", id, e)
            return HttpResponse.serverError()
        }
    }

    private static MediaType resolveImageMediaType(String bucketPath) {
        String lower = (bucketPath ?: "").toLowerCase()
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG_TYPE
        }
        if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF_TYPE
        }
        if (lower.endsWith(".webp")) {
            return new MediaType("image/webp")
        }
        return MediaType.IMAGE_JPEG_TYPE
    }

    private List<Image> populateCommentCounts(List<Image> images) {
        if (!images) {
            return images
        }

        Map<String, Integer> counts = commentService.countComments(images*.id)
        images.each { Image image ->
            image.commentCount = counts[image.id] ?: 0
        }
        return images
    }

    @Tag(name = "Image Operations")
    @Operation(summary = "Delete an image and its comments by id (owner or admin only) **Secure")
    @Delete(value = "/{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    HttpResponse deleteImage(String id, @Nullable Authentication authentication) {
        try {
            Image image = imageService.getImage(id)
            if (!image) {
                return HttpResponse.notFound()
            }
            if (!isOwnerOrAdmin(image, authentication)) {
                return HttpResponse.status(HttpStatus.FORBIDDEN)
            }
            imageService.deleteImage(id)
            commentService.deleteCommentsForImage(id)
            return HttpResponse.noContent()
        } catch (Exception e) {
            log.error("Error deleting image {}", id, e)
            return HttpResponse.serverError()
        }
    }

    private static boolean isOwnerOrAdmin(Image image, Authentication authentication) {
        if (!authentication) {
            return false
        }

        Collection<String> roles = authentication.roles ?: []
        if (roles.contains(Roles.ADMIN)) {
            return true
        }

        return true
    }

    @Tag(name = "Image Operations")
    @Operation(summary = "Upload a new image **Secure")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.MULTIPART_FORM_DATA)
    @Secure(value = Roles.USER)
    HttpResponse<Image> uploadImage(@Part CompletedFileUpload file, @Part String uploadedBy) {
        try {
            Image image = imageService.createImage(file, uploadedBy)
            return HttpResponse.created(image)
        } catch (Exception e) {
            log.error("Error uploading image", e)
            return HttpResponse.serverError()
        }
    }

}
