package io.github.qyvlik.springhmacrestverify.modules.hmac;

import com.google.common.collect.ImmutableList;
import org.apache.catalina.connector.RequestFacade;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;

import javax.servlet.*;
import java.io.IOException;
import java.util.List;

public class CachingRequestFilter implements Filter {

    // support method: GET, HEAD, POST, PUT, DELETE
    // not support method: CONNECT, PATCH, OPTIONS, TRACE
    private static final List<String> notSupportMethodList =
            ImmutableList.<String>builder().add("CONNECT", "OPTIONS", "TRACE", "PATCH").build();

    private static final List<String> supportMethods =
            ImmutableList.<String>builder().add("GET", "HEAD", "POST", "PUT", "DELETE").build();

    private FormHttpMessageConverter formConverter = new AllEncompassingFormHttpMessageConverter();

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException {

        if (!(servletRequest instanceof CachingRequestWrapper) && servletRequest instanceof RequestFacade) {

            ServletRequest requestWrapper = CachingRequestWrapper.Builder.create()
                    .request(servletRequest)
                    .convert(formConverter)
                    .methods(supportMethods)
                    .build();

            filterChain.doFilter(requestWrapper, servletResponse);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
