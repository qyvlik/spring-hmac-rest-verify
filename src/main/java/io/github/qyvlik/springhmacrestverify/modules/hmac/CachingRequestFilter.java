package io.github.qyvlik.springhmacrestverify.modules.hmac;

import com.google.common.collect.ImmutableList;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public class CachingRequestFilter implements Filter {

    // support method: GET, HEAD, POST, PUT, DELETE
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

            ServletRequest requestWrapper = CachingRequestWrapper.WrapperBuilder.builder()
                    .servletRequest((HttpServletRequest) servletRequest)
                    .converter(formConverter)
                    .methodList(supportMethods)
                    .isMock(mock)
                    .build()
                    .build();


            filterChain.doFilter(requestWrapper, servletResponse);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
