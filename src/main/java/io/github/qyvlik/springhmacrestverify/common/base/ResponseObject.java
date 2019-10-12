package io.github.qyvlik.springhmacrestverify.common.base;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

// http://www.jsonrpc.org/specification
public class ResponseObject<T> implements Serializable {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ResponseError error;

    public ResponseObject() {

    }

    public ResponseObject(T result) {
        this.result = result;
    }

    public ResponseObject(Integer code, String message) {
        this.error = new ResponseError(code, message);
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public ResponseError getError() {
        return error;
    }

    public void setError(ResponseError error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "ResponseObject{" +
                "result=" + result +
                ", error=" + error +
                '}';
    }
}
