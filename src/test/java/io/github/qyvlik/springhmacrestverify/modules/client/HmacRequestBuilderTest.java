package io.github.qyvlik.springhmacrestverify.modules.client;

import com.alibaba.fastjson.JSON;
import io.github.qyvlik.springhmacrestverify.common.base.ResponseObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.nio.charset.Charset;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HmacRequestBuilderTest {

    final private String accessKey = "f9ecb7d7-f5e5-40e1-bc9b-6b5e4ed6cfe0";
    final private String secretKey = "f9ecb7d7-f5e5-40e1-bc9b-6b5e4ed6cfe0";

    private OkHTTPHMACInterceptor okHTTPHMACInterceptor =
            new OkHTTPHMACInterceptor("HmacSHA256", secretKey);

    @Test
    public void test001_time_with_form_type() throws Exception {

        Request request = new Request.Builder()
                .get()
                .url(HttpUrl.parse("http://localhost:8080/api/v1/time"))
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("accessKey", accessKey)
                .addHeader("Nonce", System.currentTimeMillis() + "")
                .build();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(okHTTPHMACInterceptor).build();
        Call call = okHttpClient.newCall(request);

        Response response = call.execute();

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.body());

        ResponseObject responseObject = JSON.parseObject(response.body().string()).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());
    }

    @Test
    public void test002_head_with_form_type() throws Exception {
        Request request = HmacRequestBuilder.create()
                .method("HEAD")
                .url(HttpUrl.parse("http://localhost:8080/api/v1/time?k/1/1=1zzzdaw&zzz=111&zz&zz"))
                .contentType("application/x-www-form-urlencoded")
                .body(null)
                .charset(Charset.forName("UTF-8"))
                .headerOfAccessKey("accessKey")
                .headerOfAuthorization("Authorization")
                .headerOfNonce("nonce")
                .build(accessKey, secretKey, "HmacSHA256");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(okHTTPHMACInterceptor).build();
        Call call = okHttpClient.newCall(request);

        Response response = call.execute();

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.body());
    }

    @Test
    public void test003_post_with_form_type() throws Exception {
        Request request = HmacRequestBuilder.create()
                .method("POST")
                .url(HttpUrl.parse("http://localhost:8080/api/v1/post-form?param3=3"))
                .contentType("application/x-www-form-urlencoded")
                .body("param1=1&param2=2")
                .charset(Charset.forName("UTF-8"))
                .headerOfAccessKey("accessKey")
                .headerOfAuthorization("Authorization")
                .headerOfNonce("nonce")
                .build(accessKey, secretKey, "HmacSHA256");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(okHTTPHMACInterceptor).build();
        Call call = okHttpClient.newCall(request);

        Response response = call.execute();

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.body());

        ResponseObject responseObject = JSON.parseObject(response.body().string()).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }

    @Test
    public void test004_post_chinese_with_form_type() throws Exception {
        Request request = HmacRequestBuilder.create()
                .method("POST")
                .url(HttpUrl.parse("http://localhost:8080/api/v1/post-form?param3=3"))
                .contentType("application/x-www-form-urlencoded")
                .body("param1=1&param2=2中文")
                .charset(Charset.forName("UTF-8"))
                .headerOfAccessKey("accessKey")
                .headerOfAuthorization("Authorization")
                .headerOfNonce("nonce")
                .build(accessKey, secretKey, "HmacSHA256");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(okHTTPHMACInterceptor).build();
        Call call = okHttpClient.newCall(request);

        Response response = call.execute();

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.body());

        ResponseObject responseObject = JSON.parseObject(response.body().string()).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }

    @Test
    public void test005_put_chinese_with_form_type() throws Exception {
        Request request = HmacRequestBuilder.create()
                .method("PUT")
                .url(HttpUrl.parse("http://localhost:8080/api/v1/put-form?param3=3"))
                .contentType("application/x-www-form-urlencoded")
                .body("param1=1&param2=2中文")
                .charset(Charset.forName("UTF-8"))
                .headerOfAccessKey("accessKey")
                .headerOfAuthorization("Authorization")
                .headerOfNonce("nonce")
                .build(accessKey, secretKey, "HmacSHA256");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(okHTTPHMACInterceptor).build();
        Call call = okHttpClient.newCall(request);

        Response response = call.execute();

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.body());

        ResponseObject responseObject = JSON.parseObject(response.body().string()).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文, param3: 3";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }

    @Test
    public void test006_delete_chinese_with_form_type() throws Exception {
        Request request = HmacRequestBuilder.create()
                .method("DELETE")
                .url(HttpUrl.parse("http://localhost:8080/api/v1/delete-form?param3=3"))
                .contentType("application/x-www-form-urlencoded")
                .body("param1=1&param2=2中文")
                .charset(Charset.forName("UTF-8"))
                .headerOfAccessKey("accessKey")
                .headerOfAuthorization("Authorization")
                .headerOfNonce("nonce")
                .build(accessKey, secretKey, "HmacSHA256");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(okHTTPHMACInterceptor).build();
        Call call = okHttpClient.newCall(request);

        Response response = call.execute();

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.body());

        ResponseObject responseObject = JSON.parseObject(response.body().string()).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }

    @Test
    public void test007_post_chinese2_with_form_type() throws Exception {
        Request request = HmacRequestBuilder.create()
                .method("POST")
                .url(HttpUrl.parse("http://localhost:8080/api/v1/post-form-2?param3=3中文"))
                .contentType("application/x-www-form-urlencoded")
                .body("param1=1&param2=2中文")
                .charset(Charset.forName("UTF-8"))
                .headerOfAccessKey("accessKey")
                .headerOfAuthorization("Authorization")
                .headerOfNonce("nonce")
                .build(accessKey, secretKey, "HmacSHA256");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(okHTTPHMACInterceptor).build();
        Call call = okHttpClient.newCall(request);

        Response response = call.execute();

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.body());

        ResponseObject responseObject = JSON.parseObject(response.body().string()).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文, param3: 3中文";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }


    @Test
    public void test008_post_chinese_with_json_type() throws Exception {
        Request request = HmacRequestBuilder.create()
                .method("POST")
                .url(HttpUrl.parse("http://localhost:8080/api/v1/post-json?param3=3中文"))
                .contentType("application/json;charset=UTF-8")
                .body("{\"param1\": \"1\",\"param2\": \"2中文\"}")
                .charset(Charset.forName("UTF-8"))
                .headerOfAccessKey("accessKey")
                .headerOfAuthorization("Authorization")
                .headerOfNonce("nonce")
                .build(accessKey, secretKey, "HmacSHA256");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(okHTTPHMACInterceptor).build();
        Call call = okHttpClient.newCall(request);

        Response response = call.execute();

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.body());

        ResponseObject responseObject = JSON.parseObject(response.body().string()).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文, param3: 3中文";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }

    @Test
    public void test009_delete_chinese_with_json_type() throws Exception {
        Request request = HmacRequestBuilder.create()
                .method("DELETE")
                .url(HttpUrl.parse("http://localhost:8080/api/v1/delete-json?param3=3中文"))
                .contentType("application/json;charset=UTF-8")
                .body("{\"param1\": \"1\",\"param2\": \"2中文\"}")
                .charset(Charset.forName("UTF-8"))
                .headerOfAccessKey("accessKey")
                .headerOfAuthorization("Authorization")
                .headerOfNonce("nonce")
                .build(accessKey, secretKey, "HmacSHA256");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(okHTTPHMACInterceptor).build();
        Call call = okHttpClient.newCall(request);

        Response response = call.execute();

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.body());

        ResponseObject responseObject = JSON.parseObject(response.body().string()).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文, param3: 3中文";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }

    @Test
    public void test010_put_chinese_with_json_type() throws Exception {
        Request request = HmacRequestBuilder.create()
                .method("PUT")
                .url(HttpUrl.parse("http://localhost:8080/api/v1/put-json?param3=3中文"))
                .contentType("application/json;charset=UTF-8")
                .body("{\"param1\": \"1\",\"param2\": \"2中文\"}")
                .charset(Charset.forName("UTF-8"))
                .headerOfAccessKey("accessKey")
                .headerOfAuthorization("Authorization")
                .headerOfNonce("nonce")
                .build(accessKey, secretKey, "HmacSHA256");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(okHTTPHMACInterceptor).build();
        Call call = okHttpClient.newCall(request);

        Response response = call.execute();

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.body());

        ResponseObject responseObject = JSON.parseObject(response.body().string()).toJavaObject(ResponseObject.class);
        Assert.assertNotNull(responseObject);
        Assert.assertNull(responseObject.getError());

        final String resultString = "param1: 1, param2: 2中文, param3: 3中文";

        Assert.assertEquals(responseObject.getResult().toString(), resultString);
    }


}