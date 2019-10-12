package io.github.qyvlik.springhmacrestverify.modules.hmac;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class CachingRequestFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException {

        ServletRequest requestWrapper = null;

        if (servletRequest instanceof HttpServletRequest) {
            requestWrapper = new CachingRequestWrapper((HttpServletRequest) servletRequest);

            filterChain.doFilter(requestWrapper, servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
