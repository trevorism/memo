package com.trevorism.service

import com.trevorism.http.HeadersHttpResponse
import com.trevorism.http.util.CleanUrl
import com.trevorism.http.util.HeadersHttpClientResponseHandler
import com.trevorism.http.util.InvalidRequestException
import com.trevorism.https.token.ObtainTokenStrategy
import org.apache.hc.client5.http.HttpResponseException
import org.apache.hc.client5.http.classic.methods.*
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.HttpEntity
import org.apache.hc.core5.http.io.HttpClientResponseHandler
import org.apache.hc.core5.http.io.entity.EntityUtils

class MultipartHttpClient {

    protected CloseableHttpClient httpClient = HttpClients.createDefault()
    private final ObtainTokenStrategy obtainTokenStrategy

    MultipartHttpClient(ObtainTokenStrategy obtainTokenStrategy) {
        this.obtainTokenStrategy = obtainTokenStrategy
    }

    String get(String url) {
        return get(url, new HashMap<>()).getValue()
    }

    byte[] getBytes(String url) {
        HttpGet httpGet = new HttpGet(CleanUrl.startWithHttps(url))
        httpGet.setHeader("Authorization", "Bearer " + this.obtainTokenStrategy.getToken())
        HttpClientResponseHandler<byte[]> handler = { response ->
            HttpEntity entity = response.getEntity()
            return entity ? EntityUtils.toByteArray(entity) : null
        }
        try {
            return httpClient.execute(httpGet, handler)
        } catch (HttpResponseException e) {
            throw new InvalidRequestException(e, e.getStatusCode())
        } catch (Exception ex) {
            throw new InvalidRequestException(ex)
        }
    }

    HeadersHttpResponse get(String url, Map<String, String> headers) {
        Map<String, String> headersMap = this.createHeaderMap(headers)
        return requestData(new HttpGet(CleanUrl.startWithHttps(url)), headersMap)
    }

    String post(String url, byte[] content, String fileName) {
        return post(url, content, fileName, new HashMap<>()).getValue()
    }

    HeadersHttpResponse post(String url, byte[] content, String fileName, Map<String, String> headers) {
        Map<String, String> headersMap = this.createHeaderMap(headers)
        return requestData(new HttpPost(CleanUrl.startWithHttps(url)), content, fileName, headersMap)
    }

    String put(String url, byte[] content, String fileName) {
        return put(url, content, fileName, new HashMap<>()).getValue()
    }

    HeadersHttpResponse put(String url, byte[] content, String fileName, Map<String, String> headers) {
        Map<String, String> headersMap = this.createHeaderMap(headers)
        return requestData(new HttpPut(CleanUrl.startWithHttps(url)), content, fileName, headersMap)
    }

    String patch(String url, byte[] content, String fileName) {
        return patch(url, content, fileName, new HashMap<>()).getValue()
    }

    HeadersHttpResponse patch(String url, byte[] content, String fileName, Map<String, String> headers) {
        Map<String, String> headersMap = this.createHeaderMap(headers)
        return requestData(new HttpPatch(CleanUrl.startWithHttps(url)), content, fileName, headersMap)
    }

    String delete(final String url) {
        return delete(url, new HashMap<>()).getValue()
    }

    HeadersHttpResponse delete(String url, Map<String, String> headers) {
        Map<String, String> headersMap = this.createHeaderMap(headers)
        return requestData(new HttpDelete(CleanUrl.startWithHttps(url)), headersMap)
    }

    private HeadersHttpResponse requestData(HttpUriRequestBase requestType, Map<String, String> headers) {
        try {
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                requestType.setHeader(headerEntry.getKey(), headerEntry.getValue());
            }
            return httpClient.execute(requestType, new HeadersHttpClientResponseHandler());
        } catch (HttpResponseException e) {
            throw new InvalidRequestException(e, e.getStatusCode());
        } catch (Exception ex) {
            throw new InvalidRequestException(ex);
        }
    }

    private HeadersHttpResponse requestData(HttpUriRequestBase requestType, byte[] content, String fileName, Map<String, String> headers) {
        try {
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addBinaryBody("file", content, ContentType.APPLICATION_OCTET_STREAM, fileName)
                    .build()

            requestType.setEntity(entity)
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                requestType.setHeader(headerEntry.getKey(), headerEntry.getValue());
            }
            return httpClient.execute(requestType, new HeadersHttpClientResponseHandler())
        } catch (HttpResponseException e) {
            throw new InvalidRequestException(e, e.getStatusCode());
        } catch (Exception ex) {
            throw new InvalidRequestException(ex);
        }
    }

    protected Map<String, String> createHeaderMap(Map<String, String> existingHeaders) {
        Map<String, String> headersMap = new HashMap(existingHeaders)
        if (headersMap.containsKey("Authorization")) {
            headersMap.remove("Authorization")
        }
        headersMap.put("Authorization", "Bearer " + this.obtainTokenStrategy.getToken())
        return headersMap
    }

    ObtainTokenStrategy getObtainTokenStrategy() {
        return obtainTokenStrategy
    }

}
