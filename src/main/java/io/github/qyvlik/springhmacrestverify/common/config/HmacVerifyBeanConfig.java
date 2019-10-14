package io.github.qyvlik.springhmacrestverify.common.config;

import io.github.qyvlik.springhmacrestverify.common.properties.HmacVerifyProperties;
import io.github.qyvlik.springhmacrestverify.modules.hmac.CachingRequestFilter;
import io.github.qyvlik.springhmacrestverify.modules.verify.interceptor.HmacInterceptor;
import io.github.qyvlik.springhmacrestverify.modules.verify.provider.CredentialsProvider;
import io.github.qyvlik.springhmacrestverify.modules.verify.provider.impl.CredentialsProviderMapImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@EnableConfigurationProperties(HmacVerifyProperties.class)
public class HmacVerifyBeanConfig {

    private HmacVerifyProperties hmacVerifyProperties;

    public HmacVerifyBeanConfig(HmacVerifyProperties hmacVerifyProperties) {
        this.hmacVerifyProperties = hmacVerifyProperties;
    }

    @Bean
    public FilterRegistrationBean cachingRequestFilterRegister() {
        FilterRegistrationBean<CachingRequestFilter> registration = new FilterRegistrationBean<>();

        registration.setName("cachingRequestFilter");
        registration.setFilter(new CachingRequestFilter());
        registration.addUrlPatterns("/api/v1/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);          // first filter

        return registration;
    }

    @Bean
    public CredentialsProvider credentialsProvider() {

        return CredentialsProviderMapImpl.Builder
                .create()
                .put(hmacVerifyProperties.getAccessKey(), hmacVerifyProperties.getSecretKey())
                .builder();
    }

    @Bean
    public HmacInterceptor hmacInterceptor(
            @Autowired @Qualifier("credentialsProvider") CredentialsProvider credentialsProvider) {
        return new HmacInterceptor(
                credentialsProvider,
                hmacVerifyProperties.getHeader().getNonce(),
                hmacVerifyProperties.getHeader().getAccessKey(),
                hmacVerifyProperties.getHeader().getAuthorization()
        );
    }
}
