package io.github.qyvlik.springhmacrestverify.modules.client;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OkHTTPHMACInterceptor implements Interceptor {

    private String algorithm;
    private String secretKey;

    private static String signature(String algorithm, String plainText, String secretKey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac digest = Mac.getInstance(algorithm);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), algorithm);
        digest.init(secretKeySpec);
        digest.update(plainText.getBytes());
        final byte[] signatureBytes = digest.doFinal();
        digest.reset();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(hmacSignature(chain));
    }

    private Request hmacSignature(Chain chain) throws IOException {
        Request request = chain.request();
        final String DELIMITER = "\n";
        final String method = request.method();
        final String scheme = request.url().scheme();
        final String host = request.url().host();
        final String path = request.url().encodedPath();
        final String query = StringUtils.isBlank(request.url().query()) ? "" : "?" + request.url().query();
        final String contentType = getContentType(request);
        final String body = getRawContentFromBody(request.body());
        final String nonce = System.currentTimeMillis() + "";
        final String plainText = method + DELIMITER +
                scheme + DELIMITER +
                host + DELIMITER +
                path + DELIMITER +
                query + DELIMITER +
                contentType + DELIMITER +
                body + DELIMITER +
                nonce;
        String signatureStr = "";
        try {
            signatureStr = signature(algorithm, plainText, getSecretKey());
        } catch (Exception e) {
            log.error("{} failure : {}", algorithm, e.getMessage());
        }
        final String authorization = algorithm + ": " + signatureStr;
        Map<String, String> sign = Maps.newHashMap();
        sign.put("plainText", plainText);
        sign.put("signature", signatureStr);
        log.debug("sign:{}", JSON.toJSONString(sign));
        return request.newBuilder()
                .removeHeader("Nonce")
                .addHeader("Nonce", nonce)
                .removeHeader("Authorization")
                .addHeader("Authorization", authorization)
                .build();
    }

    private String getRawContentFromBody(RequestBody body) throws IOException {
        if (body != null) {
            Buffer buffer = new Buffer();
            body.writeTo(buffer);


            return buffer.readString(Charsets.UTF_8);
        }
        return "";
    }

    private String getContentType(Request request) {
        if (request.body() != null
                && request.body().contentType() != null) {
            return request.body().contentType().toString();
        }
        return request.header("Content-Type");
    }
}