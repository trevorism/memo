package com.trevorism.controller

import io.micronaut.core.io.ResourceResolver
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test

class NotFoundRouteControllerTest {

    private NotFoundRouteController controller() {
        NotFoundRouteController controller = new NotFoundRouteController()
        controller.resourceResolver = new ResourceResolver()
        controller
    }

    @Test
    void missingAssetReturnsNotFoundEvenWhenBrowserAcceptsHtml() {
        // A hashed asset from an older build must 404, not fall back to index.html --
        // otherwise the browser gets HTML for a .js module and blocks it on MIME type.
        def request = HttpRequest.GET("/assets/index-deadbeef.js").accept(MediaType.TEXT_HTML)
        assert controller().forward(request).status() == HttpStatus.NOT_FOUND
    }

    @Test
    void missingAssetReturnsNotFoundForModuleRequest() {
        // Module fetches send Accept: */*, not text/html. Previously this returned null
        // -> a default application/json error body served under a .js URL.
        def request = HttpRequest.GET("/assets/index-deadbeef.js").accept(MediaType.ALL)
        assert controller().forward(request).status() == HttpStatus.NOT_FOUND
    }

    @Test
    void missingFileWithExtensionReturnsNotFound() {
        def request = HttpRequest.GET("/favicon.ico").accept(MediaType.TEXT_HTML)
        assert controller().forward(request).status() == HttpStatus.NOT_FOUND
    }

    @Test
    void nonHtmlRequestForRouteReturnsNotFound() {
        def request = HttpRequest.GET("/some/route").accept(MediaType.APPLICATION_JSON)
        assert controller().forward(request).status() == HttpStatus.NOT_FOUND
    }

    @Test
    void spaRouteForwardsToIndexHtml() {
        // A client-side navigation route (no file extension) with a browser Accept header
        // still serves the SPA shell so deep links work. Skipped if the frontend bundle
        // hasn't been copied onto the classpath yet (e.g. a clean build before copyFrontEnd).
        Assumptions.assumeTrue(new ResourceResolver().getResource("classpath:public/index.html").isPresent())
        def request = HttpRequest.GET("/some/route").accept(MediaType.TEXT_HTML)
        def response = controller().forward(request)
        assert response.status() == HttpStatus.OK
        assert response.body() != null
    }
}
