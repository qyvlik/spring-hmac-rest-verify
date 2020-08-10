package io.github.qyvlik.springhmacrestverify.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hmac-verify")
@Data
public class HmacVerifyProperties {
    private Header header;
    private Server server;
    private String accessKey;
    private String secretKey;

    @Data
    public static class Header {
        private String nonce;
        private String accessKey;
        private String authorization;
    }

    @Data
    public static class Server {
        private String scheme;
        private String host;

    }
}
