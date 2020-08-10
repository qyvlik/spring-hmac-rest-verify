# spring-hmac-rest-verify

Another spring HMAC authentication filter for RESTfull webservice example.

Support http method: **GET**, **HEAD**, **POST**, **PUT**, **DELETE**, support Content-Type: **application/x-www-form-urlencoded**, **application/json**

You can define the `NONCE`, `ACCESS-KEY`, `AUTHORIZATION` header as follow, also can define the `server.scheme`, `server.host`, `server.port`.

```yaml
hmac-verify:
  header:
    nonce: nonce
    access-key: accesskey
    authorization: authorization
  server:
    scheme: http
    host: localhost
    port: 8080
```

Both client and server digest of the following string:

```text
METHOD\n
SCHEME\n
HOST\n
PATH\n
QUERY\n
CONTENT-TYPE\n
PAYLOAD\n
NONCE
```

- `METHOD`: http method, such as **GET**, **POST**
- `SCHEME`: http or https
- `HOST`: `localhost` or other domain name.
- `PATH`: http uri
- `QUERY`: http query string
- `CONTENT-TYPE`: content-type, support **application/x-www-form-urlencoded** or **application/json**
- `PAYLOAD`: form format or json format
- `NONCE`: nonce

## main code

See the package `io.github.qyvlik.springhmacrestverify.modules.hmac` 
and `io.github.qyvlik.springhmacrestverify.modules.verify`.

- `CachingRequestFilter` : read the payload from request, so you don't need sorting the form-data.
- `HmacSignatureHelper`: build hmac signature from `HttpServletRequestWrapper`.
- `HmacInterceptor`: verify the client signature

## example

### server side

See the package `io.github.qyvlik.springhmacrestverify.modules.verify`.

- `HmacInterceptor`: verify the client signature, you can consult it for you own server.
- `CredentialsProviderMapImpl`: simple provider for access-key, secret-key.

### client side by okhttp

```yaml
String accessKey = "f9ecb7d7-f5e5-40e1-bc9b-6b5e4ed6cfe0";
String secretKey = "f9ecb7d7-f5e5-40e1-bc9b-6b5e4ed6cfe0";
Request request = HmacRequestBuilder.create()
        .method("GET")
        .url(HttpUrl.parse("http://localhost:8080/api/v1/time"))
        .contentType("application/x-www-form-urlencoded")
        .body(null)
        .charset(Charset.forName("UTF-8"))
        .headerOfAccessKey("accessKey")
        .headerOfAuthorization("Authorization")
        .headerOfNonce("nonce")
        .build(accessKey, secretKey, "HmacSHA256");

OkHttpClient okHttpClient = new OkHttpClient();
Call call = okHttpClient.newCall(request);

Response response = call.execute();

Assert.assertNotNull(response);
Assert.assertNotNull(response.body());
```

See the more examples in [HmacRequestBuilderTest.java](src/test/java/io/github/qyvlik/springhmacrestverify/modules/client/HmacRequestBuilderTest.java).

### test case

See more test cases in [SpringHmacRestVerifyApplicationTests.java](src/test/java/io/github/qyvlik/springhmacrestverify/SpringHmacRestVerifyApplicationTests.java).

## same code

[kpavlov/spring-hmac-rest](https://github.com/kpavlov/spring-hmac-rest)

## ref

[spring boot 学习笔记(5) 解决HttpServletRequest inputStream只能读取一次的问题](https://my.oschina.net/serge/blog/1094063)

[springboot-拦截器-过滤器-Required request body is missing 异常](https://blog.csdn.net/qq_33517683/article/details/78593487)

[spring boot 配置Filter过滤器方法总结](https://blog.csdn.net/testcs_dn/article/details/80265196)

[SpringMVC 中 request.getInputStream() 为空解惑](https://emacsist.github.io/2017/12/04/springmvc-%E4%B8%AD-request.getinputstream-%E4%B8%BA%E7%A9%BA%E8%A7%A3%E6%83%91/)

[HTTP 请求方法](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Methods)

- [http PATCH](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Methods/PATCH)

- [http PUT](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Methods/PUT)

- [http POST](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Methods/POST)
    
[HTTP请求方法：GET、HEAD、POST、PUT、DELETE、CONNECT、OPTIONS、TRACE](https://itbilu.com/other/relate/EkwKysXIl.html)

[Spring security not calling my custom authentication filter when running JUnit tests](https://stackoverflow.com/questions/30478876/spring-security-not-calling-my-custom-authentication-filter-when-running-junit-t)

[使用了https后，还有必要对数据进行签名来确保数据没有被篡改吗？](https://www.zhihu.com/question/52392988)