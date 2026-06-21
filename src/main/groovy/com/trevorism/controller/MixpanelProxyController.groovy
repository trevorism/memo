package com.trevorism.controller

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.net.http.HttpClient
import java.net.http.HttpRequest as UpstreamRequest
import java.net.http.HttpResponse as UpstreamResponse
import java.time.Duration

@Controller("/mp")
@ExecuteOn(TaskExecutors.BLOCKING)
class MixpanelProxyController {

    private static final Logger log = LoggerFactory.getLogger(MixpanelProxyController)
    private static final String UPSTREAM = "https://api.mixpanel.com"

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build()

    @Post(value = "/{+path}", consumes = MediaType.ALL, produces = MediaType.ALL)
    HttpResponse<byte[]> proxyPost(HttpRequest<?> request, @Body byte[] body) {
        forward("POST", request, body ?: new byte[0])
    }

    @Get(value = "/{+path}", produces = MediaType.ALL)
    HttpResponse<byte[]> proxyGet(HttpRequest<?> request) {
        forward("GET", request, null)
    }

    private HttpResponse<byte[]> forward(String method, HttpRequest<?> request, byte[] body) {
        String path = request.path.replaceFirst("^/mp/?", "")
        String query = request.uri.rawQuery
        URI target = URI.create("${UPSTREAM}/${path}" + (query ? "?${query}" : ""))

        UpstreamRequest.Builder builder = UpstreamRequest.newBuilder(target)
                .timeout(Duration.ofSeconds(10))

        // Preserve the original client IP so Mixpanel geolocates the user, not App Engine.
        String clientIp = resolveClientIp(request)
        if (clientIp) {
            builder.header("X-Forwarded-For", clientIp)
        }

        if (method == "POST") {
            String contentType = request.headers.get(HttpHeaders.CONTENT_TYPE) ?: MediaType.APPLICATION_FORM_URLENCODED
            builder.header(HttpHeaders.CONTENT_TYPE, contentType)
            builder.POST(UpstreamRequest.BodyPublishers.ofByteArray(body))
        } else {
            builder.GET()
        }

        try {
            UpstreamResponse<byte[]> upstream = httpClient.send(builder.build(), UpstreamResponse.BodyHandlers.ofByteArray())
            String upstreamContentType = upstream.headers().firstValue("content-type").orElse(MediaType.TEXT_PLAIN)
            return HttpResponse.status(HttpStatus.valueOf(upstream.statusCode()))
                    .body(upstream.body())
                    .contentType(upstreamContentType)
        } catch (Exception e) {
            log.warn("Failed to proxy Mixpanel request to {}: {}", target, e.message)
            return HttpResponse.status(HttpStatus.BAD_GATEWAY)
        }
    }

    private static String resolveClientIp(HttpRequest<?> request) {
        String forwarded = request.headers.get("X-Forwarded-For")
        if (forwarded) {
            return forwarded.split(",")[0].trim()
        }
        return request.remoteAddress?.address?.hostAddress
    }
}
