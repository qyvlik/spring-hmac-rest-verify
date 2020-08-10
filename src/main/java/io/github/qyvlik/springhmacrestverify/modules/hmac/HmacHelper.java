package io.github.qyvlik.springhmacrestverify.modules.hmac;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Slf4j
@Builder
public class HmacHelper {
    private static final String URL_DECODE_METHOD = "POST";
    private CachingRequestWrapper request;
    private String serverScheme;
    private String serverHost;
    private String encoding;

    public HmacSignature createHmacSignatureBuilder(String nonce) {
        try {

            PlainText plainText = PlainText.builder()
                    .method(getRequest().getMethod())
                    .scheme(getServerScheme())
                    .host(getServerHost())
                    .contentType(getRequest().getContentType() == null ? "" : getRequest().getContentType())
                    .path(getRequest().getRequestURI())
                    .query(getQueryString())
                    .body(getBody())
                    .nonce(nonce)
                    .build();

            return HmacSignature.builder()
                    .plainText(plainText)
                    .build()
                    ;
        } catch (IOException e) {
            log.error("createBuilderFromRequest failure :{}", e.getMessage());
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

    private String getEncoding() {
        return this.encoding;
    }

    /**
     * querystring if not blank, start with '?'
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    private String getQueryString() throws UnsupportedEncodingException {
        return StringUtils.isBlank(getRequest().getQueryString())
                ? ""
                : "?" + URLDecoder.decode(getRequest().getQueryString(), getEncoding());
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

            // don't encoding
            return body;
//            return URLDecoder.decode(body, getEncoding());
        }
        return body;
    }
}
