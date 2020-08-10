package io.github.qyvlik.springhmacrestverify.modules.hmac;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlainText {
    public static final String DELIMITER = "\n";

    private String method;
    private String scheme;
    private String host;
    private String path;
    private String query;
    private String contentType;
    private String body;
    private String nonce;

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getMethod()).append(DELIMITER)
                .append(this.getScheme()).append(DELIMITER)
                .append(this.getHost()).append(DELIMITER)
                .append(this.getPath()).append(DELIMITER)
                .append(this.getQuery()).append(DELIMITER)
                .append(this.getContentType()).append(DELIMITER)
                .append(this.getBody()).append(DELIMITER)
                .append(this.getNonce());
        return sb.toString();
    }
}
