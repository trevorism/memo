package com.trevorism.service

import com.trevorism.https.SecureHttpClient
import com.trevorism.model.RegistrationRequest
import org.junit.jupiter.api.Test

/**
 * Hermetic: the SecureHttpClient is faked. Validation-failure cases use a client that
 * throws on any call, proving those paths short-circuit before touching the network.
 */
class DefaultRegistrationServiceTest {

    @Test
    void testRejectsShortUsername() {
        def service = buildService(failIfCalled())
        assert !service.registerUser(new RegistrationRequest(username: "ab", password: "secret1", email: "a@b.com"))
    }

    @Test
    void testRejectsInvalidEmail() {
        def service = buildService(failIfCalled())
        assert !service.registerUser(new RegistrationRequest(username: "alice", password: "secret1", email: "no-at-sign"))
    }

    @Test
    void testRejectsShortPassword() {
        def service = buildService(failIfCalled())
        assert !service.registerUser(new RegistrationRequest(username: "alice", password: "short", email: "a@b.com"))
    }

    @Test
    void testRegistersValidUser() {
        def service = buildService([post: { String u, String b -> '{"username":"alice"}' }] as SecureHttpClient)
        assert service.registerUser(new RegistrationRequest(username: "alice", password: "secret1", email: "a@b.com"))
    }

    @Test
    void testReturnsFalseWhenServerReturnsNullUser() {
        def service = buildService([post: { String u, String b -> '{}' }] as SecureHttpClient)
        assert !service.registerUser(new RegistrationRequest(username: "alice", password: "secret1", email: "a@b.com"))
    }

    @Test
    void testReturnsFalseOnException() {
        def service = buildService([post: { String u, String b -> throw new RuntimeException("boom") }] as SecureHttpClient)
        assert !service.registerUser(new RegistrationRequest(username: "alice", password: "secret1", email: "a@b.com"))
    }

    private static DefaultRegistrationService buildService(SecureHttpClient client) {
        DefaultRegistrationService service = new DefaultRegistrationService()
        service.secureHttpClient = client
        return service
    }

    private static SecureHttpClient failIfCalled() {
        return { Object... args -> throw new IllegalStateException("network call in unit test") } as SecureHttpClient
    }

}
