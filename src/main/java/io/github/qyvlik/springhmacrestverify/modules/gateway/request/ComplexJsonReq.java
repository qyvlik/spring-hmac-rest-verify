package io.github.qyvlik.springhmacrestverify.modules.gateway.request;

import lombok.Data;

import java.util.List;

@Data
public class ComplexJsonReq {
    private String param1;
    private List<String> list;
}
