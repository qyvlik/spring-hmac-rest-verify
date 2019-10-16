package io.github.qyvlik.springhmacrestverify.modules.hmac;

import com.google.common.collect.ImmutableList;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;

import javax.servlet.*;
import java.io.IOException;
import java.util.List;

public class CachingRequestFilter implements Filter {

    // support method: GET, HEAD, POST, PUT, DELETE
    // not support method: CONNECT, PATCH, OPTIONS, TRACE
    private static final List<String> notSupportMethos =
            ImmutableList.<String>builder().add("CONNECT", "OPTIONS", "TRACE", "PATCH").build();

    private static final List<String> supportMethods =
            ImmutableList.<String>builder().add("GET", "HEAD", "POST", "PUT", "DELETE").build();

    private boolean mock = false;
    private FormHttpMessageConverter formConverter = new AllEncompassingFormHttpMessageConverter();

    public static CachingRequestFilter createMockCachingRequestFilter() {
        CachingRequestFilter filter = new CachingRequestFilter();
        filter.mock = true;
        return filter;
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException {

        if (!(servletRequest instanceof CachingRequestWrapper)) {

            ServletRequest requestWrapper = CachingRequestWrapper.Builder.create()
                    .request(servletRequest)
                    .convert(formConverter)
                    .methods(supportMethods)
                    .mock(mock)
                    .build();

            filterChain.doFilter(requestWrapper, servletResponse);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
