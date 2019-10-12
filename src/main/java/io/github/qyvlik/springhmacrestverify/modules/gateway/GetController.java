package io.github.qyvlik.springhmacrestverify.modules.gateway;

import io.github.qyvlik.springhmacrestverify.common.base.ResponseObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetController {
    @RequestMapping(
            method = RequestMethod.GET,
            value = "api/v1/time",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject<Long> getTime() {
        return new ResponseObject<>(System.currentTimeMillis());
    }
}
