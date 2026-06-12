package com.trevorism.filter

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.ResponseFilter
import io.micronaut.http.annotation.ServerFilter

/**
 * Controls browser caching of the bundled SPA so new deployments render without a manual refresh.
 *
 * index.html is revalidated on every request so it always points at the current build's hashed
 * assets, while the content-hashed /assets/* files are cached indefinitely (their names change
 * each build, so a stale copy can never be referenced).
 */
@ServerFilter("/**")
class CacheControlFilter {

    private static final String IMMUTABLE = "public, max-age=31536000, immutable"
    private static final String NO_CACHE = "no-cache, must-revalidate"

    @ResponseFilter
    void applyCacheControl(HttpRequest<?> request, MutableHttpResponse<?> response) {
        String path = request.path
        if (path.startsWith("/assets/")) {
            setCacheControl(response, IMMUTABLE)
        } else if (acceptsHtml(request)) {
            // Decide off the request: the response content type is not yet populated when this
            // filter runs for static/streamed file responses, so isHtml(response) would miss.
            setCacheControl(response, NO_CACHE)
        }
    }

    private static boolean acceptsHtml(HttpRequest<?> request) {
        request.getHeaders().accept().stream().anyMatch { it.name.contains(MediaType.TEXT_HTML) }
    }

    private static void setCacheControl(MutableHttpResponse<?> response, String value) {
        response.getHeaders().remove(HttpHeaders.CACHE_CONTROL)
        response.getHeaders().add(HttpHeaders.CACHE_CONTROL, value)
    }
}
