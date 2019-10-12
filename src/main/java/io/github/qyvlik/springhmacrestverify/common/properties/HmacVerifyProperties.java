package io.github.qyvlik.springhmacrestverify.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hmac-verify")
public class HmacVerifyProperties {
    private Header header;
    private String accessKey;
    private String secretKey;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public static class Header {
        private String nonce;
        private String accessKey;
        private String authorization;

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getAuthorization() {
            return authorization;
        }

        public void setAuthorization(String authorization) {
            this.authorization = authorization;
        }
    }
}
