package io.github.qyvlik.springhmacrestverify.modules.hmac;

import org.apache.catalina.connector.CoyoteInputStream;
import org.apache.catalina.connector.InputBuffer;
import org.apache.catalina.connector.RequestFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * not work
 */
@Deprecated
public class CachingRequestWrapperHelper {

    private static final Logger logger = LoggerFactory.getLogger(CachingRequestWrapperHelper.class);

    private static final Field COYOTE_REQUEST_FIELD;
    private static final Field REQUEST_USING_INPUT_STREAM_FIELD;
    private static final Field REQUEST_INPUT_STREAM_FIELD;
    private static final Method COYOTE_INPUT_STREAM_CLEAR_METHOD;
    private static final Field REQUEST_INPUT_BUFFER_FIELD;



    static {
        Field requestField = ReflectionUtils.findField(org.apache.catalina.connector.RequestFacade.class, "request");
        Assert.state(requestField != null, "Incompatible Tomcat implementation");
        ReflectionUtils.makeAccessible(requestField);
        COYOTE_REQUEST_FIELD = requestField;

        Field usingInputStreamField = ReflectionUtils.findField(org.apache.catalina.connector.Request.class, "usingInputStream");
        Assert.state(usingInputStreamField != null, "Incompatible Tomcat implementation");
        ReflectionUtils.makeAccessible(usingInputStreamField);
        REQUEST_USING_INPUT_STREAM_FIELD = usingInputStreamField;

        Field inputStreamField = ReflectionUtils.findField(org.apache.catalina.connector.Request.class, "inputStream");
        Assert.state(inputStreamField != null, "Incompatible Tomcat implementation");
        ReflectionUtils.makeAccessible(inputStreamField);
        REQUEST_INPUT_STREAM_FIELD = inputStreamField;

        Field inputBufferStreamField = ReflectionUtils.findField(org.apache.catalina.connector.Request.class, "inputBuffer");
        Assert.state(inputBufferStreamField != null, "Incompatible Tomcat implementation");
        ReflectionUtils.makeAccessible(inputBufferStreamField);
        REQUEST_INPUT_BUFFER_FIELD = inputBufferStreamField;


        Method clearMethod = ReflectionUtils.findMethod(CoyoteInputStream.class, "clear");
        Assert.state(clearMethod != null, "Incompatible Tomcat implementation");
        ReflectionUtils.makeAccessible(clearMethod);
        COYOTE_INPUT_STREAM_CLEAR_METHOD = clearMethod;
    }

    public static InputBuffer copyInputBuffer(RequestFacade requestFacade) {
        InputBuffer copyInputBuffer = new InputBuffer();
        org.apache.catalina.connector.Request connectorRequest = (org.apache.catalina.connector.Request)
                ReflectionUtils.getField(COYOTE_REQUEST_FIELD, requestFacade);
        if (connectorRequest == null) {
            logger.error("resetInputStreamForRequest failure : get Request failure");
            return null;
        }

        // connectorRequest.inputBuffer.reset();
        InputBuffer inputBuffer = (InputBuffer) ReflectionUtils.getField(REQUEST_INPUT_BUFFER_FIELD, connectorRequest);
        if (inputBuffer == null) {
            logger.error("resetInputStreamForRequest failure : Request's inputBuffer is null");
            return null;
        }



        return copyInputBuffer;
    }

    public static void markInputStreamForRequest(RequestFacade requestFacade, int readAheadLimit) throws IOException {
        org.apache.catalina.connector.Request connectorRequest = (org.apache.catalina.connector.Request)
                ReflectionUtils.getField(COYOTE_REQUEST_FIELD, requestFacade);
        if (connectorRequest == null) {
            logger.error("resetInputStreamForRequest failure : get Request failure");
            return;
        }

        // connectorRequest.inputBuffer.reset();
        InputBuffer inputBuffer = (InputBuffer) ReflectionUtils.getField(REQUEST_INPUT_BUFFER_FIELD, connectorRequest);
        if (inputBuffer == null) {
            logger.error("resetInputStreamForRequest failure : Request's inputBuffer is null");
            return;
        }
        inputBuffer.mark(readAheadLimit);
    }

    public static void resetInputStreamForRequest(RequestFacade requestFacade) throws IOException {
        // same as follow code
        // connectorRequest.usingInputStream = false;
        // connectorRequest.inputStream.clear();
        // connectorRequest.inputStream = null;
        // connectorRequest.inputBuffer.reset();

        org.apache.catalina.connector.Request connectorRequest = (org.apache.catalina.connector.Request)
                ReflectionUtils.getField(COYOTE_REQUEST_FIELD, requestFacade);
        if (connectorRequest == null) {
            logger.error("resetInputStreamForRequest failure : get Request failure");
            return;
        }

        // connectorRequest.usingInputStream = false;
        ReflectionUtils.setField(REQUEST_USING_INPUT_STREAM_FIELD, connectorRequest, false);

        CoyoteInputStream inputStream = (CoyoteInputStream) ReflectionUtils.getField(REQUEST_INPUT_STREAM_FIELD, connectorRequest);
        if (inputStream == null) {
            logger.error("resetInputStreamForRequest failure : Request's inputStream is null");
            return;
        }

        // invoke connectorRequest.inputStream.clear()
        ReflectionUtils.invokeMethod(COYOTE_INPUT_STREAM_CLEAR_METHOD, inputStream);

        // connectorRequest.inputStream = null;
        ReflectionUtils.setField(REQUEST_INPUT_STREAM_FIELD, connectorRequest, null);

        // connectorRequest.inputBuffer.reset();
        InputBuffer inputBuffer = (InputBuffer) ReflectionUtils.getField(REQUEST_INPUT_BUFFER_FIELD, connectorRequest);
        if (inputBuffer == null) {
            logger.error("resetInputStreamForRequest failure : Request's inputBuffer is null");
            return;
        }
        inputBuffer.reset();
    }

}
