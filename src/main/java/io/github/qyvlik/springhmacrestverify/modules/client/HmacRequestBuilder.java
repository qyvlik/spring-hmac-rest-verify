package io.github.qyvlik.springhmacrestverify.modules.client;

import io.github.qyvlik.springhmacrestverify.modules.hmac.HmacSignatureBuilder;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

public class HmacRequestBuilder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private HttpUrl url;
    private String method;
    private Headers.Builder headers = new Headers.Builder();
    private String contentType;
    private RequestBody body;
    private Charset charset;
    private String headerOfNonce;
    private String headerOfAccessKey;
    private String headerOfAuthorization;

    private HmacRequestBuilder() {

    }

    public static HmacRequestBuilder create() {
        return new HmacRequestBuilder();
    }

    private static String readContentFromBody(RequestBody body, Charset charset) throws IOException {
        if (body == null) {
            return "";
        }
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        try {
            return buffer.readString(charset);
        } finally {
            buffer.close();
        }
    }


    public HmacRequestBuilder method(String method) {
        this.method = method;
        return this;
    }

    public HmacRequestBuilder url(HttpUrl url) {
        this.url = url;
        return this;
    }

    public HmacRequestBuilder contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public HmacRequestBuilder body(String bodyContent) {
        if (bodyContent != null) {
            this.body = RequestBody.create(null, bodyContent);
        }
        return this;
    }

    public HmacRequestBuilder charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public HmacRequestBuilder headerOfNonce(String headerOfNonce) {
        this.headerOfNonce = headerOfNonce;
        return this;
    }

    public HmacRequestBuilder headerOfAccessKey(String headerOfAccessKey) {
        this.headerOfAccessKey = headerOfAccessKey;
        return this;
    }

    public HmacRequestBuilder headerOfAuthorization(String headerOfAuthorization) {
        this.headerOfAuthorization = headerOfAuthorization;
        return this;
    }

    public HmacRequestBuilder header(String name, String value) {
        headers.set(name, value);
        return this;
    }

    public HmacRequestBuilder addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public HmacRequestBuilder removeHeader(String name) {
        headers.removeAll(name);
        return this;
    }

    public Request build(String accessKey, String secretKey, String algorithm) throws IOException {
        String nonce = System.currentTimeMillis() + "";
        HmacSignatureBuilder signatureBuilder = HmacSignatureBuilder.create()
                .method(method)
                .scheme(url.scheme())
                .host(url.host())
                .port(url.port())
                .contentType(contentType)
                .path(url.encodedPath())
                .query(url.query() != null ? url.query() : "")
                .body(readContentFromBody(body, charset))
                .nonce(nonce);

        String signature = signatureBuilder.signature(secretKey, algorithm);
        String authorization = algorithm + ":" + signature;

        logger.debug("plainText:{}", signatureBuilder.plaintext());

        headers.add(this.headerOfNonce, nonce);
        headers.add(this.headerOfAccessKey, accessKey);
        headers.add(this.headerOfAuthorization, authorization);
        headers.add("Content-Type", contentType);

        return new Request.Builder().url(url)
                .method(method, body)
                .headers(headers.build())
                .build();
    }
}
