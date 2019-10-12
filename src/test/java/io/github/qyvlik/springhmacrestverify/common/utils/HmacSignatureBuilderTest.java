package io.github.qyvlik.springhmacrestverify.common.utils;

import io.github.qyvlik.springhmacrestverify.modules.hmac.HmacSignatureBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HmacSignatureBuilderTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void signatureGet() throws Exception {
        HmacSignatureBuilder builder = HmacSignatureBuilder.create()
                .method("GET")
                .scheme("http")
                .host("localhost")
                .port(8080)
                .contentType("application/x-www-form-urlencoded")
                .path("api/v1/time")
                .query("")
                .body("")
                .nonce("1");

        String plaintext = builder.plaintext();

        String secretKey = "f9ecb7d7-f5e5-40e1-bc9b-6b5e4ed6cfe0";

        logger.info("plaintext:{}", plaintext);

        logger.info("signature base64:{}", builder.signature(secretKey, "HmacSHA256"));
    }

    @Test
    public void signaturePost() throws Exception {
        HmacSignatureBuilder builder = HmacSignatureBuilder.create()
                .method("POST")
                .scheme("http")
                .host("localhost")
                .port(8080)
                .contentType("application/x-www-form-urlencoded")
                .path("api/v1/time")
                .query("key1=1")
                .body("key2=2")
                .nonce("1");

        String plaintext = builder.plaintext();

        String secretKey = "f9ecb7d7-f5e5-40e1-bc9b-6b5e4ed6cfe0";

        logger.info("plaintext:{}", plaintext);

        logger.info("signature base64:{}", builder.signature(secretKey, "HmacSHA256"));
    }

}