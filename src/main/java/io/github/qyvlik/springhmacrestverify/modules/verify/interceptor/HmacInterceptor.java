package io.github.qyvlik.springhmacrestverify.modules.verify.interceptor;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.qyvlik.springhmacrestverify.common.base.ResponseObject;
import io.github.qyvlik.springhmacrestverify.common.utils.ServletUtils;
import io.github.qyvlik.springhmacrestverify.modules.hmac.CachingRequestWrapper;
import io.github.qyvlik.springhmacrestverify.modules.hmac.HmacHelper;
import io.github.qyvlik.springhmacrestverify.modules.hmac.HmacSignature;
import io.github.qyvlik.springhmacrestverify.modules.verify.provider.CredentialsProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class HmacInterceptor implements HandlerInterceptor {

    public static final List<String> notSupportMethods =
            ImmutableList.<String>builder().add("CONNECT", "OPTIONS", "TRACE", "PATCH").build();
    public static final List<String> supportMethods =
            ImmutableList.<String>builder().add("GET", "HEAD", "POST", "PUT", "DELETE").build();

    public static final String UTF8 = "UTF-8";
    public static final Map<String, String> algorithmMap =
            ImmutableMap.<String, String>builder()
                    .put("HmacSHA256".toLowerCase(), "HmacSHA256")
                    .put("HmacSHA512".toLowerCase(), "HmacSHA512")
                    .build();

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Config config;

    public HmacInterceptor(Config config) {
        this.config = config;
    }

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String httpMethod = request.getMethod();
        if (!supportMethods.contains(httpMethod)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20405, request.getRequestURI() + " not support http method : " + httpMethod);
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 405);
            return false;
        }

        String accessKey = request.getHeader(config.getHeaderOfAccessKey());
        if (StringUtils.isBlank(accessKey)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " need " + config.getHeaderOfAccessKey() + " header");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        String authorization = request.getHeader(config.getHeaderOfAuthorization());
        if (StringUtils.isBlank(authorization)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " need " + config.getHeaderOfAuthorization() + " header");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        String nonce = request.getHeader(config.getHeaderOfNonce());
        if (StringUtils.isBlank(nonce)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20400, request.getRequestURI() + " need " + config.getHeaderOfNonce() + " header");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 400);
            return false;
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
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 500);
            return false;
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
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 500);
            return false;
        }

        CredentialsProvider.Credential credential = config.getProvider().getCredential(accessKey);
        if (credential == null || StringUtils.isBlank(credential.getSecretKey())) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header "
                    + config.getHeaderOfAccessKey() + " value is invalidate");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        String[] authArray = authorization.split(":");
        if (authArray.length != 2) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header "
                    + config.getHeaderOfAuthorization() + " value is invalidate");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }
        String algorithm = authArray[0].trim();
        String clientSignature = authArray[1].trim();

        if (StringUtils.isBlank(algorithm)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header "
                    + config.getHeaderOfAuthorization() + " value is invalidate, lost algorithm");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        if (!algorithmMap.containsKey(algorithm.toLowerCase())) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header "
                    + config.getHeaderOfAuthorization() + " value is invalidate, algorithm: " + algorithm + " not support");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        if (StringUtils.isBlank(clientSignature)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header "
                    + config.getHeaderOfAuthorization() + " value is invalidate, lost signature");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        hmacSignature.setAlgorithm(algorithmMap.get(algorithm.toLowerCase()));
        hmacSignature.setSecretKey(credential.getSecretKey());
        String serverSignature = hmacSignature.signature();

        if (StringUtils.isBlank(serverSignature)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20500, request.getRequestURI() + " signature failure");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 500);
            return false;
        }

        Map<String, String> sign = Maps.newHashMap();
        sign.put("plainText", hmacSignature.plaintext());
        sign.put("signature", serverSignature);
        sign.put("client", clientSignature);

        logger.debug("sign:{}", JSON.toJSONString(sign));

        if (!serverSignature.equals(clientSignature)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20500, request.getRequestURI() + " verify signature failure");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 500);
            return false;
        }

        return true;
    }

    /**
     * HMAC Interceptor config
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Config {
        private CredentialsProvider provider;
        private String headerOfNonce;
        private String headerOfAccessKey;
        private String headerOfAuthorization;
        private String serverScheme;
        private String serverHost;
    }
}
