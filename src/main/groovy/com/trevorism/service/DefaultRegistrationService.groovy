package com.trevorism.service

import com.google.gson.Gson
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.RegistrationRequest
import com.trevorism.model.User
import jakarta.inject.Inject
import jakarta.inject.Named
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@jakarta.inject.Singleton
class DefaultRegistrationService implements RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(DefaultRegistrationService.class.getName())
    public static final String BASE_AUTH_URL = "https://auth.trevorism.com"

    @Inject
    @Named("appSecureHttpClient")
    private SecureHttpClient secureHttpClient

    private final Gson gson = new Gson()

    @Override
    boolean registerUser(RegistrationRequest registrationRequest) {
        if (!validate(registrationRequest)) {
            return false
        }
        try {
            String json = gson.toJson(registrationRequest)
            String response = secureHttpClient.post("$BASE_AUTH_URL/user", json)
            User user = gson.fromJson(response, User)
            return !User.isNullUser(user)
        } catch (Exception e) {
            log.error("Unable to register user", e)
            return false
        }
    }

    private static boolean validate(RegistrationRequest r) {
        if (!(r?.username?.length() >= 3)) return false
        if (!(r?.email?.contains("@"))) return false
        if (!(r?.password?.length() >= 6)) return false
        return true
    }
}

