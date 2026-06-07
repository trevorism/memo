package com.trevorism.controller

import com.trevorism.model.ImageComment
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.CommentService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller('/api/comment')
class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController)

    @Inject
    CommentService commentService

    @Tag(name = 'Comment Operations')
    @Operation(summary = 'List comments for an image, newest first **Secure')
    @Get(value = '/{imageId}/comments', produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    List<ImageComment> listComments(String imageId) {
        return commentService.listComments(imageId)
    }

    @Tag(name = 'Comment Operations')
    @Operation(summary = 'Add a comment to an image **Secure')
    @Post(value = '/{imageId}/comments', consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    ImageComment addComment(String imageId, @Body ImageComment comment) {
        try {
            commentService.addComment(imageId, comment)
        } catch (Exception e) {
            log.error('Error creating comment for image {}', imageId, e)
            return HttpResponse.serverError()
        }
    }

}
