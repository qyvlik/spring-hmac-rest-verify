package io.github.qyvlik.springhmacrestverify.modules.hmac;

import org.apache.catalina.connector.RequestFacade;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;


// https://github.com/kpavlov/spring-hmac-rest/blob/master/src/main/java/com/github/kpavlov/restws/server/hmac/CachingRequestWrapper.java
public class CachingRequestWrapper extends HttpServletRequestWrapper {

    private static final Field COYOTE_REQUEST_FIELD;
    private static final Field REQUEST_POST_DATA_FIELD;

    static {
        Field requestField = ReflectionUtils.findField(RequestFacade.class, "request");
        Assert.state(requestField != null, "Incompatible Tomcat implementation");
        ReflectionUtils.makeAccessible(requestField);
        COYOTE_REQUEST_FIELD = requestField;

        Field postDataField = ReflectionUtils.findField(org.apache.catalina.connector.Request.class, "postData");
        Assert.state(postDataField != null, "Incompatible Tomcat implementation");
        ReflectionUtils.makeAccessible(postDataField);
        REQUEST_POST_DATA_FIELD = postDataField;
    }

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

    private static byte[] getPostData(RequestFacade requestFacade) {
        org.apache.catalina.connector.Request connectorRequest = (org.apache.catalina.connector.Request)
                ReflectionUtils.getField(COYOTE_REQUEST_FIELD, requestFacade);
        return (byte[])
                ReflectionUtils.getField(REQUEST_POST_DATA_FIELD, connectorRequest);
    }

    private byte[] getPayload() throws IOException {
        if (payload == null) {
            int contentLength = getRequest().getContentLength();
            if (contentLength > 0) {
                payload = StreamUtils.copyToByteArray(getRequest().getInputStream());
                //if (payload.length == 0) {
                //     int catch_here = 0;
                // payload = Arrays.copyOf(getPostData(((RequestFacade) getRequest())), contentLength);
                // }
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