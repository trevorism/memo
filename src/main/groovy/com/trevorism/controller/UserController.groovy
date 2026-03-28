package com.trevorism.controller

import com.trevorism.model.RegistrationRequest
import com.trevorism.service.RegistrationService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.apache.hc.client5.http.HttpResponseException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/user")
class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController)

    @Inject
    private RegistrationService registrationService

    @Tag(name = "User Operations")
    @Operation(summary = "Register a new user on Memowand")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    boolean register(@Body RegistrationRequest registrationRequest) {
        boolean result = registrationService.registerUser(registrationRequest)
        if (!result) {
            throw new HttpResponseException(400, "Unable to register user successfully")
        }
        return result
    }
}

