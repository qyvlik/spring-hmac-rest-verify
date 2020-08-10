package io.github.qyvlik.springhmacrestverify.modules.verify.provider.impl;

import com.google.common.collect.Maps;
import io.github.qyvlik.springhmacrestverify.modules.hmac.HmacCredentialsProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Builder
public class CredentialsProviderMapImpl implements HmacCredentialsProvider {

    @Singular
    private Map<String, String> credentials = Maps.newConcurrentMap();

    @Override
    public Credential getCredential(String accessKey) {
        if (StringUtils.isBlank(accessKey)) {
            return null;
        }
        String secretKey = credentials.get(accessKey);
        if (StringUtils.isBlank(secretKey)) {
            return null;
        }
        return new CredentialImpl(accessKey, secretKey);
    }

    @AllArgsConstructor
    @Data
    public static class CredentialImpl implements Credential {
        private String accessKey;
        private String secretKey;
    }
}
