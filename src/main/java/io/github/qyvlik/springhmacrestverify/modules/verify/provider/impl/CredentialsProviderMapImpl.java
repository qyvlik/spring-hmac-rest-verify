package io.github.qyvlik.springhmacrestverify.modules.verify.provider.impl;

import com.google.common.collect.Maps;
import io.github.qyvlik.springhmacrestverify.modules.hmac.CredentialsProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Builder
public class CredentialsProviderMapImpl implements CredentialsProvider {

    @Singular
    private Map<String, String> credentials = Maps.newConcurrentMap();

    // expire time, unit is second
    private Integer expire;

    @Override
    public Credential getCredential(String accessKey) {
        if (StringUtils.isBlank(accessKey)) {
            return null;
        }
        String secretKey = credentials.get(accessKey);
        if (StringUtils.isBlank(secretKey)) {
            return null;
        }
        return new CredentialImpl(accessKey, secretKey, new NonceCheckerForExpire(expire));
    }

    @AllArgsConstructor
    @Data
    public static class CredentialImpl implements Credential {
        private String accessKey;
        private String secretKey;
        private NonceChecker nonceChecker;
    }

    @Slf4j
    @AllArgsConstructor
    public static class NonceCheckerForExpire implements NonceChecker {
        private Integer expire;

        @Override
        public boolean check(String nonce) {
            if (expire == null || expire < 0) {
                log.error("check nonce failure : expire:{}", expire);
                return false;
            }
            long now = System.currentTimeMillis();
            Long req = null;
            try {
                req = Long.parseLong(nonce);
            } catch (Exception e) {
                log.error("check nonce failure : not number nonce:{}", nonce);
                return false;
            }
            long expireTime = req + expire * 1000L;
            return now <= expireTime;
        }
    }
}
