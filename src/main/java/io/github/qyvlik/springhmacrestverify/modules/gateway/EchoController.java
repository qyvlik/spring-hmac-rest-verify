package io.github.qyvlik.springhmacrestverify.modules.gateway;

import io.github.qyvlik.springhmacrestverify.common.base.ResponseObject;
import io.github.qyvlik.springhmacrestverify.modules.gateway.request.PostRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class EchoController {

    @RequestMapping(
            method = RequestMethod.POST,
            value = "api/v1/post-form",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject<String> echoPostForm(HttpServletRequest request, String param1, String param2) {
        return new ResponseObject<>("param1: " + param1 + ", param2: " + param2);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "api/v1/post-form-2",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject<String> echoPostForm2(PostRequest postRequest, String param3) {
        return new ResponseObject<>("param1: " + postRequest.getParam1()
                + ", param2: " + postRequest.getParam2() + ", param3: " + param3);
    }

    @RequestMapping(
            method = RequestMethod.PUT,
            value = "api/v1/put-form",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject<String> echoPutForm(HttpServletRequest request, String param1, String param2, String param3) {
        return new ResponseObject<>("param1: " + param1 + ", param2: " + param2 + ", param3: " + param3);
    }

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "api/v1/delete-form",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject<String> echoDeleteForm(HttpServletRequest request, String param1, String param2) {
        return new ResponseObject<>("param1: " + param1 + ", param2: " + param2);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "api/v1/post-json",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject<String> echoPostJSON(HttpServletRequest request, @RequestBody PostRequest postRequest, String param3) {
        return new ResponseObject<>("param1: " + postRequest.getParam1()
                + ", param2: " + postRequest.getParam2() + ", param3: " + param3);
    }

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "api/v1/delete-json",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject<String> echoDeleteJSON(@RequestBody PostRequest postRequest, String param3) {
        return new ResponseObject<>("param1: " + postRequest.getParam1()
                + ", param2: " + postRequest.getParam2() + ", param3: " + param3);
    }

    @RequestMapping(
            method = RequestMethod.PUT,
            value = "api/v1/put-json",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject<String> echoPutJSON(@RequestBody PostRequest postRequest, String param3) {
        return new ResponseObject<>("param1: " + postRequest.getParam1()
                + ", param2: " + postRequest.getParam2() + ", param3: " + param3);
    }
}
