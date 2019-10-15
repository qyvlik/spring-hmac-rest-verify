package io.github.qyvlik.springhmacrestverify.modules.hmac;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class HmacSignatureHelper {

    private static final String URL_DECODE_METHOD = "POST";
    private final Logger logger = LoggerFactory.getLogger(HmacSignatureHelper.class);
    private CachingRequestWrapper request;
    private String serverScheme;
    private String serverHost;
    private Integer serverPort;
    private String encoding;

    private HmacSignatureHelper() {

    }

    public HmacSignatureBuilder createHmacSignatureBuilder(String nonce) {
        try {
            HmacSignatureBuilder builder = HmacSignatureBuilder.create();

            builder.method(request.getMethod())
                    .scheme(getServerScheme())
                    .host(getServerHost())
                    .port(getServerPort())
                    .contentType(request.getContentType() == null ? "" : request.getContentType())
                    .path(request.getRequestURI())
                    .query(getQueryString())
                    .body(getBody())
                    .nonce(nonce);

            return builder;
        } catch (IOException e) {
            logger.error("createBuilderFromRequest failure :{}", e.getMessage());
        }
        return null;
    }

    private CachingRequestWrapper getRequest() {
        return this.request;
    }

    private String getServerScheme() {
        if (this.serverScheme != null) {
            return this.serverScheme;
        }
        return getRequest().getScheme();
    }

    private String getServerHost() {
        if (this.serverHost != null) {
            return this.serverHost;
        }
        String host = getRequest().getHeader("host");
        if (StringUtils.isNotBlank(host)) {
            host = host.replaceAll(":" + getRequest().getServerPort(), "");
        }
        return host;
    }

    private Integer getServerPort() {
        if (this.serverPort != null) {
            return this.serverPort;
        }
        return getRequest().getServerPort();
    }

    private String getEncoding() {
        return this.encoding;
    }

    private String getQueryString() throws UnsupportedEncodingException {
        return getRequest().getQueryString() == null
                ? ""
                : URLDecoder.decode(getRequest().getQueryString(), getEncoding());
    }

    private String getBody() throws IOException {
        String body = "";
        byte[] content = getRequest().getContentAsByteArray();
        if (content != null && content.length > 0) {
            body = new String(content);
        }
        MediaType mediaType = MediaType.parseMediaType(getRequest().getContentType());
        if (StringUtils.isNotBlank(body)
                && URL_DECODE_METHOD.equalsIgnoreCase(getRequest().getMethod())
                && MediaType.APPLICATION_FORM_URLENCODED.includes(mediaType)) {
            return URLDecoder.decode(body, getEncoding());
        }
        return body;
    }

    public static class Builder {
        private HmacSignatureHelper helper;

        private Builder() {
            helper = new HmacSignatureHelper();
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder request(CachingRequestWrapper request) {
            helper.request = request;
            return this;
        }

        public Builder serverScheme(String serverScheme) {
            helper.serverScheme = serverScheme;
            return this;
        }

        public Builder serverHost(String serverHost) {
            helper.serverHost = serverHost;
            return this;
        }

        public Builder serverPort(Integer serverPort) {
            helper.serverPort = serverPort;
            return this;
        }

        public Builder encoding(String encoding) {
            helper.encoding = encoding;
            return this;
        }

        public HmacSignatureHelper build() {
            return helper;
        }
    }
}
