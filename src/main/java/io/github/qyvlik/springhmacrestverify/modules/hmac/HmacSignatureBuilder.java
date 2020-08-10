package io.github.qyvlik.springhmacrestverify.modules.hmac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.DecimalMin;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Deprecated
public class HmacSignatureBuilder {

    public static final String DELIMITER = "\n";
    private final Logger logger = LoggerFactory.getLogger(HmacSignatureBuilder.class);
    private PlainText plainText;

    private HmacSignatureBuilder(PlainText plainText) {
        this.plainText = plainText;
    }

    public static HmacSignatureBuilder create() {
        return new HmacSignatureBuilder(new PlainText());
    }

    public static String signature(String algorithm, String plainText, String secretKey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac digest = Mac.getInstance(algorithm);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), algorithm);
        digest.init(secretKeySpec);
        digest.update(plainText.getBytes());

        final byte[] signatureBytes = digest.doFinal();
        digest.reset();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    public String plaintext() {
        StringBuffer sb = new StringBuffer();
        sb.append(plainText.getMethod()).append(DELIMITER)
                .append(plainText.getScheme()).append(DELIMITER)
                .append(plainText.getHost()).append(DELIMITER)
                .append(plainText.getPort()).append(DELIMITER)
                .append(plainText.getPath()).append(DELIMITER)
                .append(plainText.getQuery()).append(DELIMITER)
                .append(plainText.getContentType()).append(DELIMITER)
                .append(plainText.getBody()).append(DELIMITER)
                .append(plainText.getNonce());
        return sb.toString();
    }

    public String signature(String secretKey, String algorithm) {
        try {
            return signature(algorithm, plaintext(), secretKey);
        } catch (NoSuchAlgorithmException e) {
            logger.error("signature failure no such algorithm:{}, error:{}", algorithm, e.getMessage());
        } catch (InvalidKeyException e) {
            logger.error("signature failure invalid key :{}, error:{}", secretKey, e.getMessage());
        } catch (Exception e) {
            logger.error("signature failure error:{}", e.getMessage());
        }
        return "";
    }

    public HmacSignatureBuilder method(String method) {
        // todo check method
        this.plainText.setMethod(method);
        return this;
    }

    public HmacSignatureBuilder scheme(String scheme) {
        this.plainText.setScheme(scheme);
        return this;
    }

    public HmacSignatureBuilder contentType(String contentType) {
        this.plainText.setContentType(contentType);
        return this;
    }

    public HmacSignatureBuilder host(String host) {
        this.plainText.setHost(host);
        return this;
    }

    public HmacSignatureBuilder port(Integer port) {
        this.plainText.setPort(port);
        return this;
    }

    public HmacSignatureBuilder path(String path) {
        this.plainText.setPath(path);
        return this;
    }

    public HmacSignatureBuilder query(String query) {
        this.plainText.setQuery(query);
        return this;
    }

    public HmacSignatureBuilder body(String body) {
        this.plainText.setBody(body);
        return this;
    }

    public HmacSignatureBuilder nonce(String nonce) {
        this.plainText.setNonce(nonce);
        return this;
    }

    private static class PlainText {
        private String method;
        private String scheme;
        private String host;
        private Integer port;
        private String path;
        private String query;
        private String contentType;
        private String body;
        private String nonce;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }
    }
}
