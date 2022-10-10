package io.github.qyvlik.springhmacrestverify.modules.hmac;

import io.github.qyvlik.springhmacrestverify.common.utils.GZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
public class HmacSignatureTest {

    @Test
    public void sign() throws InvalidKeyException, NoSuchAlgorithmException {
        String secretKey = UUID.randomUUID().toString();
        String algorithm = "HmacSHA256";
        String plainText = System.currentTimeMillis() + "";

        String sign = HmacSignature.signature(algorithm, plainText, secretKey);
        log.info("plainText:{}, secretKey:{}, sign:{}", plainText, secretKey, sign);
    }

    @Test
    public void sign2() throws InvalidKeyException, NoSuchAlgorithmException {
        String secretKey = UUID.randomUUID().toString();
        String algorithm = "HmacSHA512";
        String plainText = System.currentTimeMillis() + "";

        String sign = HmacSignature.signature(algorithm, plainText, secretKey);
        log.info("plainText:{}, secretKey:{}, sign:{}", plainText, secretKey, sign);
    }

    @Test
    public void signAndCompressByte() throws InvalidKeyException, NoSuchAlgorithmException {
        String secretKey = UUID.randomUUID().toString();
        String algorithm = "HmacSHA256";
        String plainText = System.currentTimeMillis() + "";

        byte[] signBytes = HmacSignature.signatureByte(algorithm, plainText, secretKey);
        byte[] compressSignBytes = GZipUtils.compress(signBytes);
        log.info("algorithm:{} signBytes.length:{}, compressSignBytes.length:{}", algorithm, signBytes.length, compressSignBytes.length);
    }

    @Test
    public void signAndCompressByte2() throws InvalidKeyException, NoSuchAlgorithmException {
        String secretKey = UUID.randomUUID().toString();
        String algorithm = "HmacSHA512";
        String plainText = System.currentTimeMillis() + "";

        byte[] signBytes = HmacSignature.signatureByte(algorithm, plainText, secretKey);
        byte[] compressSignBytes = GZipUtils.compress(signBytes);
        log.info("algorithm:{} signBytes.length:{}, compressSignBytes.length:{}", algorithm, signBytes.length, compressSignBytes.length);
    }
}