package io.github.qyvlik.springhmacrestverify.common.config;

import io.github.qyvlik.springhmacrestverify.modules.verify.interceptor.HmacInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class HmacVerifyConfig implements WebMvcConfigurer {

    @Autowired
    private HmacInterceptor hmacInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(hmacInterceptor)
                .addPathPatterns("/**")
        ;
    }

}
