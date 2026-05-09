package com.trevorism.controller

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Controller("/.well-known")
class WellKnownController {

    @Tag(name = "Well-Known Operations")
    @Operation(summary = "Microsoft Identity Association")
    @Get(value = "/microsoft-identity-association.json", produces = MediaType.APPLICATION_JSON)
    static Map<String, Object> microsoftIdentityAssociation() {
        return [
            associatedApplications: [
                [applicationId: "6a213614-458e-4167-a7d7-7a0b099a6e5a"]
            ]
        ]
    }
}


