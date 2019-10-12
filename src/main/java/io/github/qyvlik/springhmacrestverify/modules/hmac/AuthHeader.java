package io.github.qyvlik.springhmacrestverify.modules.hmac;

import org.apache.commons.lang3.StringUtils;

public class AuthHeader {

    public static final String DELIMITER = ":";
    private String algorithm;
    private String signature;

    public AuthHeader(String algorithm, String signature) {
        this.algorithm = algorithm;
        this.signature = signature;
    }

    public static AuthHeader parse(String headerValue) {
        if (StringUtils.isBlank(headerValue)) {
            return null;
        }
        try {
            String[] array = headerValue.split(DELIMITER);
            return new AuthHeader(array[0].trim(), array[1].trim());
        } catch (Exception e) {
            return null;
        }
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "AuthHeader{" +
                "algorithm='" + algorithm + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
