package io.github.qyvlik.springhmacrestverify.modules.hmac;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


// https://github.com/kpavlov/spring-hmac-rest/blob/master/src/main/java/com/github/kpavlov/restws/server/hmac/CachingRequestWrapper.java
public class CachingRequestWrapper extends HttpServletRequestWrapper {
    private byte[] payload;

    private MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>(0);

    CachingRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    void setFormParams(MultiValueMap<String, String> formParams) {
        if (formParams != null) {
            this.formParams = formParams;
        }
    }

    public byte[] getContentAsByteArray() throws IOException {
        return getPayload();
    }

    private byte[] getPayload() throws IOException {
        if (payload == null) {
            int contentLength = getRequest().getContentLength();
            if (contentLength > 0) {
                payload = StreamUtils.copyToByteArray(getRequest().getInputStream());
                if (payload.length != contentLength) {
                    throw new IOException("request inputStream read payload length : " + payload.length
                            + " not equals content-length : " + contentLength
                            + ", does the request inputStream already closed?");
                }
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

    @Override
    @Nullable
    public String getParameter(String name) {
        String queryStringValue = super.getParameter(name);
        String formValue = this.formParams.getFirst(name);
        return (queryStringValue != null ? queryStringValue : formValue);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> result = new LinkedHashMap<>();
        Enumeration<String> names = getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            result.put(name, getParameterValues(name));
        }
        return result;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        Set<String> names = new LinkedHashSet<>();
        names.addAll(Collections.list(super.getParameterNames()));
        names.addAll(this.formParams.keySet());
        return Collections.enumeration(names);
    }

    @Override
    @Nullable
    public String[] getParameterValues(String name) {
        String[] parameterValues = super.getParameterValues(name);
        List<String> formParam = this.formParams.get(name);
        if (formParam == null) {
            return parameterValues;
        }
        if (parameterValues == null || getQueryString() == null) {
            return StringUtils.toStringArray(formParam);
        } else {
            List<String> result = new ArrayList<>(parameterValues.length + formParam.size());
            result.addAll(Arrays.asList(parameterValues));
            result.addAll(formParam);
            return StringUtils.toStringArray(result);
        }
    }

    public static class Builder {

        private static final String parseFormBodyMethod = "POST";

        private HttpServletRequest servletRequest;
        private List<String> methodList;
        private FormHttpMessageConverter converter;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder request(ServletRequest request) {
            this.servletRequest = (HttpServletRequest) request;
            return this;
        }

        public Builder methods(List<String> methodList) {
            this.methodList = methodList;
            return this;
        }

        public Builder convert(FormHttpMessageConverter converter) {
            this.converter = converter;
            return this;
        }

        public HttpServletRequest build() throws IOException {
            String method = servletRequest.getMethod();

            if (methodList.contains(method)) {
                CachingRequestWrapper cachingRequestWrapper = new CachingRequestWrapper(servletRequest);

                cachingRequestWrapper.setFormParams(parseIfNecessary(cachingRequestWrapper));

                return cachingRequestWrapper;
            } else {
                return servletRequest;
            }
        }

        @Nullable
        private MultiValueMap<String, String> parseIfNecessary(HttpServletRequest request) throws IOException {
            if (!shouldParse(request)) {
                return null;
            }

            HttpInputMessage inputMessage = new ServletServerHttpRequest(request) {
                @Override
                public InputStream getBody() throws IOException {
                    return request.getInputStream();
                }
            };
            return converter.read(null, inputMessage);
        }

        private boolean shouldParse(HttpServletRequest request) {
            if (!parseFormBodyMethod.equals(request.getMethod())) {
                return false;
            }
            try {
                MediaType mediaType = MediaType.parseMediaType(request.getContentType());
                return MediaType.APPLICATION_FORM_URLENCODED.includes(mediaType);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
    }
}