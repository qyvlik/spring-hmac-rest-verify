package io.github.qyvlik.springhmacrestverify.modules.hmac;

/**
 * HMAC ak sk provider
 */
public interface CredentialsProvider {

    Credential getCredential(String accessKey);

    interface Credential {

        String getAccessKey();

        String getSecretKey();

        NonceChecker getNonceChecker();
    }

    interface NonceChecker {
        boolean check(String nonce);
    }
}
