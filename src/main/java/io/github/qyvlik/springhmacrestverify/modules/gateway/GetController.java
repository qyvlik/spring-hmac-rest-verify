package io.github.qyvlik.springhmacrestverify.modules.gateway;

import io.github.qyvlik.springhmacrestverify.common.base.ResponseObject;
import io.github.qyvlik.springhmacrestverify.modules.gateway.request.HeadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(
            method = RequestMethod.GET,
            value = "api/v1/time",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject<Long> getTime() {
        return new ResponseObject<>(System.currentTimeMillis());
    }

    @RequestMapping(
            method = RequestMethod.HEAD,
            value = "api/v1/head",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void head(HeadRequest request) {
        int catch_here = 0;
        logger.info("api/v1/head:{}", request);
    }

    @RequestMapping(
            method = RequestMethod.HEAD,
            value = "api/v1/head",
//            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void headWithNotFormContentType(HeadRequest request) {
        int catch_here = 0;
        logger.info("api/v1/head, with-not-form-content-type:{}", request);
    }
}
