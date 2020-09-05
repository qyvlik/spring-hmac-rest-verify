package io.github.qyvlik.springhmacrestverify;

import com.alibaba.fastjson.JSON;
import io.github.qyvlik.springhmacrestverify.common.base.ResponseObject;
import io.github.qyvlik.springhmacrestverify.common.properties.HmacVerifyProperties;
import io.github.qyvlik.springhmacrestverify.modules.hmac.CachingRequestFilter;
import io.github.qyvlik.springhmacrestverify.modules.hmac.HmacSignature;
import io.github.qyvlik.springhmacrestverify.modules.hmac.PlainText;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.net.URLDecoder;
import java.net.URLEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpringHmacRestVerifyApplicationTests {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private HmacVerifyProperties properties;

    private CachingRequestFilter cachingRequestFilter = CachingRequestFilter.createMockCachingRequestFilter();

    private MockMvc mockMvc;
    private String algorithm;

    @Before
    public void setup() {
        mockMvc = webAppContextSetup(this.context)
                .addFilter(cachingRequestFilter, "/api/v1/*")
                .build();
        algorithm = "HmacSHA256";
    }

    @Test
    public void test001_time_with_form_type() throws Exception {
        String accessKey = properties.getAccessKey();
        String secretKey = properties.getSecretKey();

        String uri = "/api/v1/time";

        String query = "k/1/1=1zzzdaw&zzz=111&zz&zz";

        String nonce = System.currentTimeMillis() + "";

        String signature = HmacSignature.builder().plainText(PlainText.builder()
                .method("GET")
                .scheme("http")
                .host("localhost")
                .path(uri)
                .query("?" + query)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body("")
                .nonce(nonce)
                .build())
                .algorithm(algorithm)
                .secretKey(secretKey)
                .build()
                .signature();

        String authorization = algorithm + ":" + signature;

        String responseString = this.mockMvc.perform(
                get(uri + "?" + query)
                        .header(properties.getHeader().getAccessKey(), accessKey)
                        .header(properties.getHeader().getAuthorization(), authorization)
                        .header(properties.getHeader().getNonce(), nonce)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        ).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();

        ResponseObject responseObject = JSON.parseObject(responseString).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        log.info("test001_time_with_form_type result:{}", responseObject.getResult());
    }

    @Test
    public void test002_head_with_form_type() throws Exception {
        String accessKey = properties.getAccessKey();
        String secretKey = properties.getSecretKey();

        String uri = "/api/v1/time";

        String query = "k/1/1=1zzzdaw&zzz=111&zz&zz";

        String nonce = System.currentTimeMillis() + "";

        String signature = HmacSignature.builder().plainText(PlainText.builder()
                .method("HEAD")
                .scheme("http")
                .host("localhost")
                .path(uri)
                .query("?" + query)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body("")
                .nonce(nonce)
                .build())
                .algorithm(algorithm)
                .secretKey(secretKey)
                .build()
                .signature();

        String authorization = algorithm + ":" + signature;

        String responseString = this.mockMvc.perform(
                head(uri + "?" + query)
                        .header(properties.getHeader().getAccessKey(), accessKey)
                        .header(properties.getHeader().getAuthorization(), authorization)
                        .header(properties.getHeader().getNonce(), nonce)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        ).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn()
                .getResponse().getContentAsString();
        Assert.assertEquals(responseString, "");
    }

    @Test
    public void test003_post_with_form_type() throws Exception {
        String accessKey = properties.getAccessKey();
        String secretKey = properties.getSecretKey();

        String uri = "/api/v1/post-form";

        String query = "param3=3";

        String body = "param1=1&param2=2";

        String nonce = System.currentTimeMillis() + "";

        String signature = HmacSignature.builder().plainText(PlainText.builder()
                .method("POST")
                .scheme("http")
                .host("localhost")
                .path(uri)
                .query("?" + query)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(body)
                .nonce(nonce)
                .build())
                .algorithm(algorithm)
                .secretKey(secretKey)
                .build()
                .signature();

        String authorization = algorithm + ":" + signature;

        String responseString = this.mockMvc.perform(
                post(uri + "?" + query)
                        .header(properties.getHeader().getAccessKey(), accessKey)
                        .header(properties.getHeader().getAuthorization(), authorization)
                        .header(properties.getHeader().getNonce(), nonce)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .content(body)
        ).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn()
                .getResponse().getContentAsString();

        ResponseObject responseObject = JSON.parseObject(responseString).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }

    @Test
    public void test004_post_chinese_with_form_type() throws Exception {
        String accessKey = properties.getAccessKey();
        String secretKey = properties.getSecretKey();

        String uri = "/api/v1/post-form";

        String query = "param3=3";

        String body = "param1=1&param2=2中文";

        String nonce = System.currentTimeMillis() + "";

        String signature = HmacSignature.builder().plainText(PlainText.builder()
                .method("POST")
                .scheme("http")
                .host("localhost")
                .path(uri)
                .query("?" + query)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(body)
                .nonce(nonce)
                .build())
                .algorithm(algorithm)
                .secretKey(secretKey)
                .build()
                .signature();

        String authorization = algorithm + ":" + signature;

        String responseString = this.mockMvc.perform(
                post(uri + "?" + query)
                        .header(properties.getHeader().getAccessKey(), accessKey)
                        .header(properties.getHeader().getAuthorization(), authorization)
                        .header(properties.getHeader().getNonce(), nonce)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .content(body)
        ).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn()
                .getResponse().getContentAsString();

        ResponseObject responseObject = JSON.parseObject(responseString).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }

    @Test
    public void test005_put_chinese_with_form_type() throws Exception {
        String accessKey = properties.getAccessKey();
        String secretKey = properties.getSecretKey();

        String uri = "/api/v1/put-form";

        String query = "param3=3";

        String body = "param1=1&param2=2中文";

        String nonce = System.currentTimeMillis() + "";

        String signature = HmacSignature.builder().plainText(PlainText.builder()
                .method("PUT")
                .scheme("http")
                .host("localhost")
                .path(uri)
                .query("?" + query)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(body)
                .nonce(nonce)
                .build())
                .algorithm(algorithm)
                .secretKey(secretKey)
                .build()
                .signature();

        String authorization = algorithm + ":" + signature;

        String responseString = this.mockMvc.perform(
                put(uri + "?" + query)
                        .header(properties.getHeader().getAccessKey(), accessKey)
                        .header(properties.getHeader().getAuthorization(), authorization)
                        .header(properties.getHeader().getNonce(), nonce)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .content(body)
        ).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn()
                .getResponse().getContentAsString();

        ResponseObject responseObject = JSON.parseObject(responseString).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文, param3: 3";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }

    @Test
    public void test006_delete_chinese_with_form_type() throws Exception {
        String accessKey = properties.getAccessKey();
        String secretKey = properties.getSecretKey();

        String uri = "/api/v1/delete-form";

        String query = "param3=3";

        String body = "param1=1&param2=2中文";

        String nonce = System.currentTimeMillis() + "";

        String signature = HmacSignature.builder().plainText(PlainText.builder()
                .method("DELETE")
                .scheme("http")
                .host("localhost")
                .path(uri)
                .query("?" + query)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(body)
                .nonce(nonce)
                .build())
                .algorithm(algorithm)
                .secretKey(secretKey)
                .build()
                .signature();

        String authorization = algorithm + ":" + signature;

        String responseString = this.mockMvc.perform(
                delete(uri + "?" + query)
                        .header(properties.getHeader().getAccessKey(), accessKey)
                        .header(properties.getHeader().getAuthorization(), authorization)
                        .header(properties.getHeader().getNonce(), nonce)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .content(body)
        ).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn()
                .getResponse().getContentAsString();

        ResponseObject responseObject = JSON.parseObject(responseString).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }

    @Test
    public void test007_post_chinese2_with_form_type() throws Exception {
        String accessKey = properties.getAccessKey();
        String secretKey = properties.getSecretKey();

        String uri = "/api/v1/post-form-2";
        String query = String.format("%s=%s",
                URLEncoder.encode("param3", "UTF-8"),
                URLEncoder.encode("3中文", "UTF-8"));

        String body = String.format("%s=%s&%s=%s",
                URLEncoder.encode("param1", "UTF-8"),
                URLEncoder.encode("1", "UTF-8"),
                URLEncoder.encode("param2", "UTF-8"),
                URLEncoder.encode("2中文", "UTF-8"));

        String nonce = System.currentTimeMillis() + "";
        PlainText plainText = PlainText.builder()
                .method("POST")
                .scheme("http")
                .host("localhost")
                .path(uri)
                .query("?" + query)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(body)
                .nonce(nonce)
                .build();
        log.info("plainText:{}", plainText);
        String signature = HmacSignature.builder()
                .plainText(plainText)
                .algorithm(algorithm)
                .secretKey(secretKey)
                .build()
                .signature();

        String authorization = algorithm + ":" + signature;

        String responseString = this.mockMvc.perform(
                post(uri + "?" + URLDecoder.decode(query, "UTF-8"))
                        .header(properties.getHeader().getAccessKey(), accessKey)
                        .header(properties.getHeader().getAuthorization(), authorization)
                        .header(properties.getHeader().getNonce(), nonce)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .content(body)
        ).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn()
                .getResponse().getContentAsString();

        ResponseObject responseObject = JSON.parseObject(responseString).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文, param3: 3中文";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }


    @Test
    public void test008_post_chinese_with_json_type() throws Exception {
        String accessKey = properties.getAccessKey();
        String secretKey = properties.getSecretKey();

        String uri = "/api/v1/post-json";

        // todo encode
        String query = "param3=3中文";

        String body = "{\"param1\": \"1\",\"param2\": \"2中文\"}";

        String nonce = System.currentTimeMillis() + "";

        String signature = HmacSignature.builder().plainText(PlainText.builder()
                .method("POST")
                .scheme("http")
                .host("localhost")
                .path(uri)
                .query("?" + query)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .body(body)
                .nonce(nonce)
                .build())
                .algorithm(algorithm)
                .secretKey(secretKey)
                .build()
                .signature();

        String authorization = algorithm + ":" + signature;

        String responseString = this.mockMvc.perform(
                post(uri + "?" + query)
                        .header(properties.getHeader().getAccessKey(), accessKey)
                        .header(properties.getHeader().getAuthorization(), authorization)
                        .header(properties.getHeader().getNonce(), nonce)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(body)
        ).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn()
                .getResponse().getContentAsString();

        ResponseObject responseObject = JSON.parseObject(responseString).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文, param3: 3中文";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }

    @Test
    public void test009_delete_chinese_with_json_type() throws Exception {
        String accessKey = properties.getAccessKey();
        String secretKey = properties.getSecretKey();

        String uri = "/api/v1/delete-json";

        // todo encode
        String query = "param3=3中文";

        String body = "{\"param1\": \"1\",\"param2\": \"2中文\"}";

        String nonce = System.currentTimeMillis() + "";

        String signature = HmacSignature.builder().plainText(PlainText.builder()
                .method("DELETE")
                .scheme("http")
                .host("localhost")
                .path(uri)
                .query("?" + query)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .body(body)
                .nonce(nonce)
                .build())
                .algorithm(algorithm)
                .secretKey(secretKey)
                .build()
                .signature();

        String authorization = algorithm + ":" + signature;

        String responseString = this.mockMvc.perform(
                delete(uri + "?" + query)
                        .header(properties.getHeader().getAccessKey(), accessKey)
                        .header(properties.getHeader().getAuthorization(), authorization)
                        .header(properties.getHeader().getNonce(), nonce)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(body)
        ).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn()
                .getResponse().getContentAsString();

        ResponseObject responseObject = JSON.parseObject(responseString).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文, param3: 3中文";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }


    @Test
    public void test010_put_chinese_with_json_type() throws Exception {
        String accessKey = properties.getAccessKey();
        String secretKey = properties.getSecretKey();

        String uri = "/api/v1/put-json";

        // todo encode
        String query = "param3=3中文";

        String body = "{\"param1\": \"1\",\"param2\": \"2中文\"}";

        String nonce = System.currentTimeMillis() + "";

        String signature = HmacSignature.builder().plainText(PlainText.builder()
                .method("PUT")
                .scheme("http")
                .host("localhost")
                .path(uri)
                .query("?" + query)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .body(body)
                .nonce(nonce)
                .build())
                .algorithm(algorithm)
                .secretKey(secretKey)
                .build()
                .signature();

        String authorization = algorithm + ":" + signature;

        String responseString = this.mockMvc.perform(
                put(uri + "?" + query)
                        .header(properties.getHeader().getAccessKey(), accessKey)
                        .header(properties.getHeader().getAuthorization(), authorization)
                        .header(properties.getHeader().getNonce(), nonce)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(body)
        ).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn()
                .getResponse().getContentAsString();

        ResponseObject responseObject = JSON.parseObject(responseString).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文, param3: 3中文";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }
}
