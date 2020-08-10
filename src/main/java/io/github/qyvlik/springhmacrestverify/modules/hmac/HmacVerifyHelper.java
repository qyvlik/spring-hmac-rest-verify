package io.github.qyvlik.springhmacrestverify.modules.hmac;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.qyvlik.springhmacrestverify.common.base.ResponseObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.List;
import java.util.Map;

@Slf4j
public class HmacVerifyHelper {
    public static final List<String> supportMethods =
            ImmutableList.<String>builder().add("GET", "HEAD", "POST", "PUT", "DELETE").build();

    public static final String UTF8 = "UTF-8";
    public static final Map<String, String> algorithmMap =
            ImmutableMap.<String, String>builder()
                    .put("HmacSHA256".toLowerCase(), "HmacSHA256")
                    .put("HmacSHA512".toLowerCase(), "HmacSHA512")
                    .build();

    public static VerifyResponse verify(HttpServletRequest request,
                                        HmacConfig config) {
        String httpMethod = request.getMethod();
        if (!supportMethods.contains(httpMethod)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20405, request.getRequestURI() + " not support http method : " + httpMethod);
            return fail(405, responseObject);
        }

        String accessKey = request.getHeader(config.getHeaderOfAccessKey());
        if (StringUtils.isBlank(accessKey)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " need " + config.getHeaderOfAccessKey() + " header");
            return fail(401, responseObject);
        }

        String authorization = request.getHeader(config.getHeaderOfAuthorization());
        if (StringUtils.isBlank(authorization)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " need " + config.getHeaderOfAuthorization() + " header");
            return fail(401, responseObject);
        }

        String nonce = request.getHeader(config.getHeaderOfNonce());
        if (StringUtils.isBlank(nonce)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20400, request.getRequestURI() + " need " + config.getHeaderOfNonce() + " header");
            return fail(400, responseObject);
        }

        CachingRequestWrapper cachingRequestWrapper = null;
        if (request instanceof HttpServletRequestWrapper) {
            if (request instanceof CachingRequestWrapper) {
                cachingRequestWrapper = (CachingRequestWrapper) request;
            } else {
                if (((HttpServletRequestWrapper) request).getRequest() instanceof CachingRequestWrapper) {
                    cachingRequestWrapper = (CachingRequestWrapper) ((HttpServletRequestWrapper) request).getRequest();
                }
            }
        }

        if (cachingRequestWrapper == null) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20500, request.getRequestURI() + " request is not CachingRequestWrapper");
            return fail(500, responseObject);
        }

        HmacSignature hmacSignature = HmacHelper.builder()
                .encoding(UTF8)
                .request(cachingRequestWrapper)
                .serverScheme(config.getServerScheme())
                .serverHost(config.getServerHost())
                .build()
                .createHmacSignatureBuilder(nonce);

        if (hmacSignature == null) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20500, request.getRequestURI()
                    + " get hmac signature builder from request failure");
            return fail(500, responseObject);
        }

        HmacCredentialsProvider.Credential credential = config.getProvider().getCredential(accessKey);
        if (credential == null || StringUtils.isBlank(credential.getSecretKey())) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header "
                    + config.getHeaderOfAccessKey() + " value is invalidate");
            return fail(401, responseObject);
        }

        String[] authArray = authorization.split(":");
        if (authArray.length != 2) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header "
                    + config.getHeaderOfAuthorization() + " value is invalidate");
            return fail(401, responseObject);
        }
        String algorithm = authArray[0].trim();
        String clientSignature = authArray[1].trim();

        if (StringUtils.isBlank(algorithm)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header "
                    + config.getHeaderOfAuthorization() + " value is invalidate, lost algorithm");
            return fail(401, responseObject);
        }

        if (!algorithmMap.containsKey(algorithm.toLowerCase())) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header "
                    + config.getHeaderOfAuthorization() + " value is invalidate, algorithm: " + algorithm + " not support");
            return fail(401, responseObject);
        }

        if (StringUtils.isBlank(clientSignature)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header "
                    + config.getHeaderOfAuthorization() + " value is invalidate, lost signature");
            return fail(401, responseObject);
        }

        hmacSignature.setAlgorithm(algorithmMap.get(algorithm.toLowerCase()));
        hmacSignature.setSecretKey(credential.getSecretKey());
        String serverSignature = hmacSignature.signature();

        if (StringUtils.isBlank(serverSignature)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20500, request.getRequestURI() + " signature failure");
            return fail(500, responseObject);
        }

        Map<String, String> sign = Maps.newHashMap();
        sign.put("plainText", hmacSignature.plaintext());
        sign.put("signature", serverSignature);
        sign.put("client", clientSignature);
        log.debug("sign:{}", JSON.toJSONString(sign));

        if (!serverSignature.equals(clientSignature)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20500, request.getRequestURI() + " verify signature failure");
            return fail(500, responseObject);
        }

        return VerifyResponse.ok();
    }

    private static VerifyResponse fail(int httpStatus, ResponseObject<String> responseObject) {
        return VerifyResponse
                .builder()
                .success(false)
                .httpStatus(httpStatus)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .body(JSON.toJSONString(responseObject))
                .build();
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class VerifyResponse {
        /**
         * verify is success
         */
        private Boolean success;
        /**
         * http status
         */
        private int httpStatus;

        /**
         * response contentType
         */
        private String contentType;

        /**
         * response body
         */
        private String body;


        public static VerifyResponse ok() {
            return VerifyResponse.builder().success(true).build();
        }
    }

}
