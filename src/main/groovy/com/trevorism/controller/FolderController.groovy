package com.trevorism.controller

import com.trevorism.model.Folder
import com.trevorism.model.Image
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.CommentService
import com.trevorism.service.FolderService
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.cookie.Cookie
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller('/api/folder')
class FolderController {

    private static final Logger log = LoggerFactory.getLogger(FolderController)

    @Inject
    FolderService folderService

    @Inject
    CommentService commentService

    @Tag(name = 'Folder Operations')
    @Operation(summary = 'List all folders, newest first **Secure')
    @Get(value = '/', produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    List<Folder> listFolders() {
        try {
            return folderService.listFolders().each { populate(it) }
        } catch (Exception e) {
            log.error('Error listing folders', e)
            throw new RuntimeException('Unable to list folders', e)
        }
    }

    @Tag(name = 'Folder Operations')
    @Operation(summary = 'Get folder metadata by id **Secure')
    @Get(value = '/{id}', produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    Folder getFolder(String id) {
        Folder folder = folderService.getFolder(id)
        if (folder) {
            populate(folder)
        }
        return folder
    }

    @Tag(name = 'Folder Operations')
    @Operation(summary = 'List the images contained in a folder, newest first **Secure')
    @Get(value = '/{id}/images', produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    HttpResponse<List<Image>> listFolderImages(String id) {
        try {
            List<Image> images = folderService.listFolderImages(id)
            if (images == null) {
                return HttpResponse.notFound()
            }
            return HttpResponse.ok(populateCommentCounts(images))
        } catch (Exception e) {
            log.error('Error listing images for folder {}', id, e)
            return HttpResponse.serverError()
        }
    }

    @Tag(name = 'Folder Operations')
    @Operation(summary = 'Get folders that contain a given image **Secure')
    @Get(value = '/forImage/{imageId}', produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    List<Folder> listFoldersForImage(String imageId) {
        try {
            return folderService.listFoldersForImage(imageId).each { populate(it) }
        } catch (Exception e) {
            log.error('Error listing folders for image {}', imageId, e)
            throw new RuntimeException('Unable to list folders for image ' + imageId, e)
        }
    }

    @Tag(name = 'Folder Operations')
    @Operation(summary = 'Create a new folder **Secure')
    @Post(value = '/', consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    HttpResponse<Folder> createFolder(@Body Map<String, String> body, @Nullable Authentication authentication) {
        try {
            String name = body?.name
            if (!name?.trim()) {
                return HttpResponse.badRequest()
            }
            String username = body?.username?.trim() ?: (authentication?.name ?: 'Unknown')
            Folder folder = folderService.createFolder(name, username)
            return HttpResponse.created(populate(folder))
        } catch (Exception e) {
            log.error('Error creating folder', e)
            return HttpResponse.serverError()
        }
    }

    @Tag(name = 'Folder Operations')
    @Operation(summary = 'Rename a folder by id **Secure')
    @Put(value = '/{id}', consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    HttpResponse<Folder> renameFolder(String id, @Body Map<String, String> body) {
        try {
            if (!body?.name?.trim()) {
                return HttpResponse.badRequest()
            }
            Folder folder = folderService.renameFolder(id, body.name)
            if (!folder) {
                return HttpResponse.notFound()
            }
            return HttpResponse.ok(populate(folder))
        } catch (Exception e) {
            log.error('Error renaming folder {}', id, e)
            return HttpResponse.serverError()
        }
    }

    @Tag(name = 'Folder Operations')
    @Operation(summary = 'Delete a folder by id (creator or admin only; images are not deleted) **Secure')
    @Delete(value = '/{id}', produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    HttpResponse deleteFolder(String id, HttpRequest<?> request, @Nullable Authentication authentication) {
        try {
            Folder folder = folderService.getFolder(id)
            if (!folder) {
                return HttpResponse.notFound()
            }
            if (!isCreatorOrAdmin(folder, request, authentication)) {
                return HttpResponse.status(HttpStatus.FORBIDDEN)
            }
            folderService.deleteFolder(id)
            return HttpResponse.noContent()
        } catch (Exception e) {
            log.error('Error deleting folder {}', id, e)
            return HttpResponse.serverError()
        }
    }

    private static boolean isCreatorOrAdmin(Folder folder, HttpRequest<?> request, Authentication authentication) {
        Collection<String> roles = authentication?.roles ?: []
        if (roles.contains(Roles.ADMIN)) {
            return true
        }

        Cookie adminCookie = request?.cookies?.get('admin')
        if (adminCookie?.value?.equalsIgnoreCase('true')) {
            return true
        }

        Cookie userCookie = request?.cookies?.get('user_name')
        String caller = userCookie?.value ?: authentication?.name
        return caller && folder.username && caller.equalsIgnoreCase(folder.username)
    }

    @Tag(name = 'Folder Operations')
    @Operation(summary = 'Add an image to a folder **Secure')
    @Post(value = '/{id}/images/{imageId}', produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    HttpResponse<Folder> addImageToFolder(String id, String imageId) {
        try {
            Folder folder = folderService.addImageToFolder(id, imageId)
            if (!folder) {
                return HttpResponse.notFound()
            }
            return HttpResponse.ok(populate(folder))
        } catch (Exception e) {
            log.error('Error adding image {} to folder {}', imageId, id, e)
            return HttpResponse.serverError()
        }
    }

    @Tag(name = 'Folder Operations')
    @Operation(summary = 'Remove an image from a folder **Secure')
    @Delete(value = '/{id}/images/{imageId}', produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    HttpResponse<Folder> removeImageFromFolder(String id, String imageId) {
        try {
            Folder folder = folderService.removeImageFromFolder(id, imageId)
            if (!folder) {
                return HttpResponse.notFound()
            }
            return HttpResponse.ok(populate(folder))
        } catch (Exception e) {
            log.error('Error removing image {} from folder {}', imageId, id, e)
            return HttpResponse.serverError()
        }
    }

    private static Folder populate(Folder folder) {
        if (!folder) {
            return folder
        }
        List<String> ids = folder.imageIds ?: []
        folder.imageCount = ids.size()
        folder.coverImageId = ids ? ids.last() : null
        return folder
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

}
