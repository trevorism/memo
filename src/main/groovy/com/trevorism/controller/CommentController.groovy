package com.trevorism.controller

import com.trevorism.model.Comment
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/comment")
class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class.name)

    @Tag(name = "Comment Operations")
    @Operation(summary = "Get all comments for an image")
    @Get(value = "/image/{imageId}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    List<Comment> listByImage(String imageId) {
        // TODO: Implement with datastore client
        return []
    }

    @Tag(name = "Comment Operations")
    @Operation(summary = "Get a comment by id")
    @Get(value = "/{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    Comment getById(String id) {
        // TODO: Implement with datastore client
        return null
    }

    @Tag(name = "Comment Operations")
    @Operation(summary = "Create a comment on an image **Secure")
    @Post(value = "/image/{imageId}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    Comment create(String imageId, @Body Comment comment) {
        // TODO: Implement with datastore client
        comment.imageId = imageId
        return comment
    }

    @Tag(name = "Comment Operations")
    @Operation(summary = "Update a comment **Secure")
    @Put(value = "/{id}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    Comment update(String id, @Body Comment comment) {
        // TODO: Implement with datastore client
        return comment
    }

    @Tag(name = "Comment Operations")
    @Operation(summary = "Delete a comment **Secure")
    @Delete(value = "/{id}", produces = MediaType.TEXT_PLAIN)
    @Secure(value = Roles.USER, allowInternal = true)
    String delete(String id) {
        // TODO: Implement with datastore client
        return "deleted"
    }
}
