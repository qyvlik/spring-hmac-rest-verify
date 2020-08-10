package io.github.qyvlik.springhmacrestverify.modules.verify.interceptor;

import io.github.qyvlik.springhmacrestverify.common.utils.ServletUtils;
import io.github.qyvlik.springhmacrestverify.modules.hmac.HmacConfig;
import io.github.qyvlik.springhmacrestverify.modules.hmac.HmacVerifyHelper;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HmacInterceptor implements HandlerInterceptor {

    private HmacConfig config;

    public HmacInterceptor(HmacConfig config) {
        this.config = config;
    }

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HmacVerifyHelper.VerifyResponse verifyResult = HmacVerifyHelper.verify(request, config);
        if (verifyResult.getSuccess()) {
            return true;
        }

        ServletUtils.writeJsonString(response, verifyResult.getBody(), verifyResult.getHttpStatus());
        return false;
    }
}
