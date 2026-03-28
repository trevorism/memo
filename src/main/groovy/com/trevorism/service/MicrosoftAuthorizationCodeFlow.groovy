package com.trevorism.service

import com.trevorism.PropertiesProvider
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Oauth2Response
import com.trevorism.model.Oauth2Tokens
import jakarta.inject.Inject
import jakarta.inject.Named

@jakarta.inject.Singleton
@Named("microsoft")
class MicrosoftAuthorizationCodeFlow implements Oauth2AuthorizationCodeFlow {

    public static final String TENANT_ID = "d77da90e-329a-41c3-b8b7-f76b8bf71b06"
    public static final String CLIENT_ID = "c3ede79b-cc30-4f21-818c-45f727113b0e"
    public static final String INSTANCE = "https://login.microsoftonline.com"
    public static final String REDIRECT_URL = "https://memowand.com/api/microsoft/callback"
    public static final String AUTH_MICROSOFT_ENDPOINT = "https://auth.trevorism.com/microsoft"

    @Inject
    private PropertiesProvider propertiesProvider

    @Inject
    @Named("appSecureHttpClient")
    private SecureHttpClient httpClient

    @Override
    String getAuthorizationUrl(String trevorismTenantGuid, String returnUrl) {
        String state = Oauth2Utils.encodeState(returnUrl, trevorismTenantGuid)
        return "${INSTANCE}/${TENANT_ID}/oauth2/v2.0/authorize?client_id=${CLIENT_ID}&response_type=code&redirect_uri=${REDIRECT_URL}&response_mode=query&scope=openid%20profile%20email&state=${state}"
    }

    @Override
    Oauth2Tokens exchangeCodeForProviderToken(String code, String state) {
        String clientSecret = propertiesProvider.getProperty("apiSecret")
        String tokenUrl = "${INSTANCE}/${TENANT_ID}/oauth2/token"
        Oauth2Response oauth2Response = Oauth2Utils.requestTokenFromOauthProvider(tokenUrl, CLIENT_ID, clientSecret, code, REDIRECT_URL)
        String trevorismTenantGuid = Oauth2Utils.extractTenantIdFromState(state)
        return Oauth2Tokens.fromOauth2Response(oauth2Response, trevorismTenantGuid)
    }

    @Override
    String getTrevorismToken(Oauth2Tokens tokens) {
        return httpClient.post(AUTH_MICROSOFT_ENDPOINT, Oauth2Utils.gson.toJson(tokens))
    }

    @Override
    Map fetchUserInfo(Oauth2Tokens tokens) {
        String tokenJson = Oauth2Utils.gson.toJson(tokens)
        String responseJson = httpClient.post("$AUTH_MICROSOFT_ENDPOINT/claims", tokenJson)
        return Oauth2Utils.gson.fromJson(responseJson, Map)
    }
}

