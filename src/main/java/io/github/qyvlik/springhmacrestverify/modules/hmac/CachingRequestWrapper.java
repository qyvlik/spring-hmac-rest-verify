package io.github.qyvlik.springhmacrestverify.modules.hmac;

import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;


// https://github.com/kpavlov/spring-hmac-rest/blob/master/src/main/java/com/github/kpavlov/restws/server/hmac/CachingRequestWrapper.java
public class CachingRequestWrapper extends HttpServletRequestWrapper {
    private byte[] payload;

    /**
     * Create a new CachingRequestWrapper for the given servlet request.
     *
     * @param request the original servlet request
     */
    public CachingRequestWrapper(HttpServletRequest request, boolean loadFirst) throws IOException {
        super(request);
        if (loadFirst) {
            getPayload();
        }
    }

    private byte[] getPayload() throws IOException {
        if (payload == null) {
            int contentLength = getRequest().getContentLength();
            if (contentLength > 0) {
                payload = StreamUtils.copyToByteArray(getRequest().getInputStream());
            }
        }
        return payload;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(getPayload());
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }
        };
    }

    public byte[] getContentAsByteArray() throws IOException {
        return getPayload();
    }

}