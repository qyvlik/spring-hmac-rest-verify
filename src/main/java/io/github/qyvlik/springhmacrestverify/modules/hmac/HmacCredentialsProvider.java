package io.github.qyvlik.springhmacrestverify.modules.hmac;

/**
 * HMAC ak sk provider
 */
public interface HmacCredentialsProvider {

    Credential getCredential(String accessKey);

    interface Credential {

        String getAccessKey();

        String getSecretKey();
    }
}
