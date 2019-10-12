package io.github.qyvlik.springhmacrestverify.modules.hmac;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthHeaderTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void parse() throws Exception {
        String headerValue = "HmacSHA256:zeFitGx/Eum1AeI8yUKD9TpCyJ7XFodgJPVFWTARWUY=";
        AuthHeader authHeader = AuthHeader.parse(headerValue);

        Assert.assertNotNull(authHeader);
        Assert.assertEquals(authHeader.getAlgorithm(), "HmacSHA256");
        Assert.assertEquals(authHeader.getSignature(), "zeFitGx/Eum1AeI8yUKD9TpCyJ7XFodgJPVFWTARWUY=");

        logger.info("parse:{}", authHeader);
    }

}