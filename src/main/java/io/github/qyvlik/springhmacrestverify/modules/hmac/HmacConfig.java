package io.github.qyvlik.springhmacrestverify.modules.hmac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HmacConfig {
    private HmacCredentialsProvider provider;
    private String headerOfNonce;
    private String headerOfAccessKey;
    private String headerOfAuthorization;
    private String serverScheme;
    private String serverHost;
}
