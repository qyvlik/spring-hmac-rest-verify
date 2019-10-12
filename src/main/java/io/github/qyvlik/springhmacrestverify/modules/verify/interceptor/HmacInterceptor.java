package io.github.qyvlik.springhmacrestverify.modules.verify.interceptor;

import com.alibaba.fastjson.JSON;
import io.github.qyvlik.springhmacrestverify.common.base.ResponseObject;
import io.github.qyvlik.springhmacrestverify.common.utils.ServletUtils;
import io.github.qyvlik.springhmacrestverify.modules.hmac.AuthHeader;
import io.github.qyvlik.springhmacrestverify.modules.hmac.CachingRequestWrapper;
import io.github.qyvlik.springhmacrestverify.modules.hmac.HmacSignatureBuilder;
import io.github.qyvlik.springhmacrestverify.modules.hmac.HmacSignatureHelper;
import io.github.qyvlik.springhmacrestverify.modules.verify.provider.CredentialsProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HmacInterceptor implements HandlerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CredentialsProvider credentialsProvider;
    private String nonceHeaderName;
    private String accessKeyHeaderName;
    private String authorizationHeaderName;

    public HmacInterceptor(CredentialsProvider credentialsProvider,
                           String nonceHeaderName,
                           String accessKeyHeaderName,
                           String authorizationHeaderName) {
        this.credentialsProvider = credentialsProvider;
        this.nonceHeaderName = nonceHeaderName;
        this.accessKeyHeaderName = accessKeyHeaderName;
        this.authorizationHeaderName = authorizationHeaderName;
    }

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String accessKey = request.getHeader(accessKeyHeaderName);
        if (StringUtils.isBlank(accessKey)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " need " + accessKeyHeaderName + " header");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        String authorization = request.getHeader(authorizationHeaderName);
        if (StringUtils.isBlank(authorization)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " need " + authorizationHeaderName + " header");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        String nonce = request.getHeader(nonceHeaderName);
        if (StringUtils.isBlank(nonce)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20400, request.getRequestURI() + " need " + nonceHeaderName + " header");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 400);
            return false;
        }

        if (!(request instanceof CachingRequestWrapper)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20500, request.getRequestURI() + " request is not CachingRequestWrapper");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 500);
            return false;
        }

        CredentialsProvider.Credential credential = credentialsProvider.getCredential(accessKey);
        if (credential == null || StringUtils.isBlank(credential.getSecretKey())) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header " + accessKeyHeaderName + " value is invalidate");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        HmacSignatureBuilder builder =
                HmacSignatureHelper.getBuilderFromRequest((CachingRequestWrapper) request, nonce);

        if (builder == null) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20500, request.getRequestURI() + " get hmac signature builder from request failure");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 500);
            return false;
        }

        AuthHeader authHeader = AuthHeader.parse(authorization);
        if (authHeader == null) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header " + authorizationHeaderName + " value is invalidate");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        if (StringUtils.isBlank(authHeader.getAlgorithm())) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header " + authorizationHeaderName + " value is invalidate, lost algorithm");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        if (StringUtils.isBlank(authHeader.getSignature())) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20401, request.getRequestURI() + " header " + authorizationHeaderName + " value is invalidate, lost signature");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 401);
            return false;
        }

        String serverSignature = builder.signature(credential.getSecretKey(), authHeader.getAlgorithm());
        if (StringUtils.isBlank(serverSignature)) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20500, request.getRequestURI() + " signature failure");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 500);
            return false;
        }

        logger.debug("plainText:{}, serverSignature:{}", builder.plaintext(), serverSignature);

        if (!serverSignature.equals(authHeader.getSignature())) {
            ResponseObject<String> responseObject = new ResponseObject<>(
                    20500, request.getRequestURI() + " verify signature failure");
            ServletUtils.writeJsonString(response, JSON.toJSONString(responseObject), 500);
            return false;
        }

        return true;
    }
}
