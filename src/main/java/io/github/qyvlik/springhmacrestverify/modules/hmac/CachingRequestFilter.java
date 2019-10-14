package io.github.qyvlik.springhmacrestverify.modules.hmac;

import com.google.common.collect.ImmutableList;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public class CachingRequestFilter implements Filter {

    private static final List<String> notSupportMethodList =
            ImmutableList.<String>builder().add("CONNECT", "OPTIONS", "TRACE", "PATCH").build();

    // other support http method
    // GET, HEAD, POST, PUT, DELETE

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException {

        ServletRequest requestWrapper = null;

        if (!(servletRequest instanceof CachingRequestWrapper)) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            String httpMethod = httpServletRequest.getMethod();

            if (!notSupportMethodList.contains(httpMethod)) {
                requestWrapper = new CachingRequestWrapper(httpServletRequest, true);
                filterChain.doFilter(requestWrapper, servletResponse);
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
