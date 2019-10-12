package io.github.qyvlik.springhmacrestverify.modules.verify.provider.impl;

import com.google.common.collect.Maps;
import io.github.qyvlik.springhmacrestverify.modules.verify.provider.CredentialsProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class CredentialsProviderMapImpl implements CredentialsProvider {

    private Map<String, String> keyMap = Maps.newConcurrentMap();

    @Override
    public Credential getCredential(String accessKey) {
        if (StringUtils.isBlank(accessKey)) {
            return null;
        }
        String secretKey = keyMap.get(accessKey);
        if (StringUtils.isBlank(secretKey)) {
            return null;
        }
        return new CredentialImpl(accessKey, secretKey);
    }

    public void putCredential(String accessKey, String secretKey) {
        if (StringUtils.isBlank(accessKey)) {
            return;
        }
        if (StringUtils.isBlank(secretKey)) {
            return;
        }

        keyMap.put(accessKey, secretKey);
    }

    public static class Builder {
        private CredentialsProviderMapImpl provider;

        public Builder(CredentialsProviderMapImpl provider) {
            this.provider = provider;
        }

        public static Builder create() {
            return new Builder(new CredentialsProviderMapImpl());
        }

        public Builder put(String accessKey, String secretKey) {
            this.provider.putCredential(accessKey, secretKey);
            return this;
        }

        public CredentialsProvider builder() {
            return this.provider;
        }
    }

    public static class CredentialImpl implements Credential {

        private String accessKey;
        private String secretKey;

        public CredentialImpl(String accessKey, String secretKey) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
        }

        @Override
        public String getAccessKey() {
            return accessKey;
        }

        @Override
        public String getSecretKey() {
            return secretKey;
        }
    }
}
