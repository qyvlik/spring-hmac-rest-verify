package io.github.qyvlik.springhmacrestverify.modules.verify.interceptor;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.qyvlik.springhmacrestverify.common.base.ResponseObject;
import io.github.qyvlik.springhmacrestverify.common.utils.ServletUtils;
import io.github.qyvlik.springhmacrestverify.modules.hmac.AuthHeader;
import io.github.qyvlik.springhmacrestverify.modules.hmac.CachingRequestWrapper;
import io.github.qyvlik.springhmacrestverify.modules.hmac.HmacHelper;
import io.github.qyvlik.springhmacrestverify.modules.hmac.HmacSignature;
import io.github.qyvlik.springhmacrestverify.modules.verify.provider.CredentialsProvider;
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
                .serverPort(config.getServerPort())
                .build()
                .createHmacSignatureBuilder(nonce);

        if (hmacSignature == null) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20500, request.getRequestURI() + " get hmac signature builder from request failure");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 500);
            return false;
        }

        CredentialsProvider.Credential credential = config.getProvider().getCredential(accessKey);
        if (credential == null || StringUtils.isBlank(credential.getSecretKey())) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header " + config.getHeaderOfAccessKey() + " value is invalidate");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        AuthHeader authHeader = AuthHeader.parse(authorization);
        if (authHeader == null) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header " + config.getHeaderOfAuthorization() + " value is invalidate");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        if (StringUtils.isBlank(authHeader.getAlgorithm())) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header " + config.getHeaderOfAuthorization() + " value is invalidate, lost algorithm");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        if (!algorithmMap.containsKey(authHeader.getAlgorithm().toLowerCase())) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header " + config.getHeaderOfAuthorization() + " value is invalidate, algorithm: " + authHeader.getAlgorithm() + " not support");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        if (StringUtils.isBlank(authHeader.getSignature())) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header " + config.getHeaderOfAuthorization() + " value is invalidate, lost signature");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        hmacSignature.setAlgorithm(algorithmMap.get(authHeader.getAlgorithm().toLowerCase()));
        hmacSignature.setSecretKey(credential.getSecretKey());
        String serverSignature = hmacSignature.signature();

        if (StringUtils.isBlank(serverSignature)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20500, request.getRequestURI() + " signature failure");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 500);
            return false;
        }

        logger.debug("plainText:{}, serverSignature:{} clientSignature:{}",
                hmacSignature.plaintext(), serverSignature, authHeader.getSignature());

        if (!serverSignature.equals(authHeader.getSignature())) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20500, request.getRequestURI() + " verify signature failure");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 500);
            return false;
        }

        return true;
    }

    public static class Config {
        private CredentialsProvider provider;
        private String headerOfNonce;
        private String headerOfAccessKey;
        private String headerOfAuthorization;
        private String serverScheme;
        private String serverHost;
        private Integer serverPort;

        public CredentialsProvider getProvider() {
            return provider;
        }

        public void setProvider(CredentialsProvider provider) {
            this.provider = provider;
        }

        public String getHeaderOfNonce() {
            return headerOfNonce;
        }

        public void setHeaderOfNonce(String headerOfNonce) {
            this.headerOfNonce = headerOfNonce;
        }

        public String getHeaderOfAccessKey() {
            return headerOfAccessKey;
        }

        public void setHeaderOfAccessKey(String headerOfAccessKey) {
            this.headerOfAccessKey = headerOfAccessKey;
        }

        public String getHeaderOfAuthorization() {
            return headerOfAuthorization;
        }

        public void setHeaderOfAuthorization(String headerOfAuthorization) {
            this.headerOfAuthorization = headerOfAuthorization;
        }

        public String getServerScheme() {
            return serverScheme;
        }

        public void setServerScheme(String serverScheme) {
            this.serverScheme = serverScheme;
        }

        public String getServerHost() {
            return serverHost;
        }

        public void setServerHost(String serverHost) {
            this.serverHost = serverHost;
        }

        public Integer getServerPort() {
            return serverPort;
        }

        public void setServerPort(Integer serverPort) {
            this.serverPort = serverPort;
        }
    }

    public static class ConfigBuilder {
        private Config config;

        private ConfigBuilder() {
            this.config = new Config();
        }

        public static ConfigBuilder create() {
            return new ConfigBuilder();
        }

        public ConfigBuilder provider(CredentialsProvider provider) {
            this.config.provider = provider;
            return this;
        }

        public ConfigBuilder headerOfNonce(String headerOfNonce) {
            this.config.headerOfNonce = headerOfNonce;
            return this;
        }

        public ConfigBuilder headerOfAccessKey(String headerOfAccessKey) {
            this.config.headerOfAccessKey = headerOfAccessKey;
            return this;
        }

        public ConfigBuilder headerOfAuthorization(String headerOfAuthorization) {
            this.config.headerOfAuthorization = headerOfAuthorization;
            return this;
        }

        public ConfigBuilder serverScheme(String serverScheme) {
            this.config.serverScheme = serverScheme;
            return this;
        }

        public ConfigBuilder serverHost(String serverHost) {
            this.config.serverHost = serverHost;
            return this;
        }

        public ConfigBuilder serverPort(Integer serverPort) {
            this.config.serverPort = serverPort;
            return this;
        }

        public Config builder() {
            return this.config;
        }
    }
}
