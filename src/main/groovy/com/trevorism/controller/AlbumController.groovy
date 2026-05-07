package com.trevorism.controller

import com.trevorism.model.Album
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/album")
class AlbumController {

    private static final Logger log = LoggerFactory.getLogger(AlbumController.class.name)

    @Tag(name = "Album Operations")
    @Operation(summary = "Get all albums for the current user")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    List<Album> listAll() {
        // TODO: Implement with datastore client
        return []
    }

    @Tag(name = "Album Operations")
    @Operation(summary = "Get an album by id")
    @Get(value = "/{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    Album getById(String id) {
        // TODO: Implement with datastore client
        return null
    }

    @Tag(name = "Album Operations")
    @Operation(summary = "Create a new album **Secure")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    Album create(@Body Album album) {
        // TODO: Implement with datastore client
        return album
    }

    @Tag(name = "Album Operations")
    @Operation(summary = "Update an album **Secure")
    @Put(value = "/{id}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    Album update(String id, @Body Album album) {
        // TODO: Implement with datastore client
        return album
    }

    @Tag(name = "Album Operations")
    @Operation(summary = "Delete an album **Secure")
    @Delete(value = "/{id}", produces = MediaType.TEXT_PLAIN)
    @Secure(value = Roles.USER, allowInternal = true)
    String delete(String id) {
        // TODO: Implement with datastore client
        return "deleted"
    }
}
