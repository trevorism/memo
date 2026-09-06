package com.trevorism.controller

import com.trevorism.http.async.AsyncHttpClient
import com.trevorism.model.ForgotPasswordRequest
import com.trevorism.model.LoginRequest
import com.trevorism.model.User
import com.trevorism.service.UserSessionService
import com.trevorism.ui.CookieDomainPolicy
import com.trevorism.ui.PublicOriginResolver
import com.trevorism.ui.UiAuthConfiguration
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.cookie.Cookie
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse
import org.apache.hc.core5.concurrent.FutureCallback
import org.junit.jupiter.api.Test

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class LoginControllerTest {

    private static final String GUID = "606db07c-3733-4697-88de-bb159773ea94"

    private static LoginController controllerFor(List<String> platformDomains) {
        UiAuthConfiguration configuration = new UiAuthConfiguration()
        configuration.setPlatformDomains(platformDomains)

        LoginController controller = new LoginController()
        controller.userSessionService = new StubUserSessionService()
        controller.publicOriginResolver = new PublicOriginResolver()
        controller.cookieDomainPolicy = new CookieDomainPolicy(configuration)
        controller.asyncHttpClient = new SilentAsyncHttpClient()
        return controller
    }

    private static Map<String, Cookie> loginAt(String host, String scheme = "https") {
        HttpResponse response = controllerFor(["memowand.com"]).login(
                new LoginRequest(username: "alice", password: "secret"),
                GUID,
                HttpRequest.POST("${scheme}://${host}/api/login/${GUID}", "").header("Host", host))
        return response.getCookies().getAll().collectEntries { [(it.name): it] }
    }

    @Test
    void testCookiesSpanTheTenantDomainInProduction() {
        Map<String, Cookie> cookies = loginAt("www.memowand.com")

        assert cookies["session"].domain == "memowand.com"
        assert cookies["refresh_token"].domain == "memowand.com"
        assert cookies["user_name"].domain == "memowand.com"
        assert cookies["admin"].domain == "memowand.com"
    }

    @Test
    void testCookiesAreHostOnlyOffTheTenantDomain() {
        Map<String, Cookie> cookies = loginAt("pr-6-dot-trevorism-memo.uk.r.appspot.com")

        assert cookies["session"].domain == null
        assert cookies["refresh_token"].domain == null
    }

    @Test
    void testCookiesAreHostOnlyInLocalDevelopment() {
        Map<String, Cookie> cookies = loginAt("localhost:8080", "http")

        assert cookies["session"].domain == null
        assert !cookies["session"].isSecure()
    }

    @Test
    void testSessionAndRefreshStayHttpOnly() {
        Map<String, Cookie> cookies = loginAt("memowand.com")

        assert cookies["session"].isHttpOnly()
        assert cookies["refresh_token"].isHttpOnly()
        assert !cookies["user_name"].isHttpOnly()
        assert cookies["session"].maxAge == 900L
        assert cookies["refresh_token"].maxAge == 86400L
    }

    private static class StubUserSessionService implements UserSessionService {
        String getToken(LoginRequest loginRequest, String guid) { return "token" }

        String getRefreshToken(LoginRequest loginRequest, String guid) { return "refresh" }

        String redeemRefreshToken(String refreshToken) { return "token" }

        User getUserFromToken(String bearerToken) { return new User(username: "alice", admin: false) }

        boolean generateForgotPasswordLink(ForgotPasswordRequest forgotPasswordRequest) { return true }

        void resetPassword(String tenantId, String resetId) {}
    }

    private static class SilentAsyncHttpClient implements AsyncHttpClient {
        Future<SimpleHttpResponse> get(String url, FutureCallback<SimpleHttpResponse> callback) { done() }

        Future<SimpleHttpResponse> post(String url, String body, FutureCallback<SimpleHttpResponse> callback) { done() }

        Future<SimpleHttpResponse> put(String url, String body, FutureCallback<SimpleHttpResponse> callback) { done() }

        Future<SimpleHttpResponse> patch(String url, String body, FutureCallback<SimpleHttpResponse> callback) { done() }

        Future<SimpleHttpResponse> delete(String url, FutureCallback<SimpleHttpResponse> callback) { done() }

        private static Future<SimpleHttpResponse> done() {
            return CompletableFuture.completedFuture(SimpleHttpResponse.create(200))
        }
    }
}
