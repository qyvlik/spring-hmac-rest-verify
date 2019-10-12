package io.github.qyvlik.springhmacrestverify.modules.verify.provider;

public interface CredentialsProvider {

    Credential getCredential(String accessKey);

    interface Credential {

        String getAccessKey();

        String getSecretKey();
    }
}
