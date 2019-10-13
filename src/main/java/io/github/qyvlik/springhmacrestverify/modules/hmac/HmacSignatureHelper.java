package io.github.qyvlik.springhmacrestverify.modules.hmac;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HmacSignatureHelper {

    private static final Logger logger = LoggerFactory.getLogger(HmacSignatureHelper.class);

    public static HmacSignatureBuilder getBuilderFromRequest(CachingRequestWrapper request, String nonce) {
        try {
            HmacSignatureBuilder builder = HmacSignatureBuilder.create();

            String body = "";
            byte[] content = request.getContentAsByteArray();
            if (content != null && content.length > 0) {
                body = new String(content);
            }

            String host = request.getHeader("host");
            if (StringUtils.isNotBlank(host)) {
                host = host.replaceAll(":" + request.getServerPort(), "");
            }

            builder.method(request.getMethod())
                    .scheme(request.getScheme())                // todo replace
                    .host(host)                                 // todo replace
                    .port(request.getServerPort())              // todo replace
                    .contentType(request.getContentType() == null ? "" : request.getContentType())
                    .path(request.getRequestURI())
                    .query(request.getQueryString() == null ? "" : request.getQueryString())
                    .body(body)
                    .nonce(nonce);

            return builder;
        } catch (IOException e) {
            logger.error("getBuilderFromRequest failure :{}", e.getMessage());
        }
        return null;
    }
}
