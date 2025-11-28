package com.nhnacademy.memberapi.dto.oauth2;

import java.util.Map;

public class PaycoResponse implements OAuth2Response{
    private final Map<String, Object> attribute;

    public PaycoResponse(Map<String, Object> allAttributes) {
        Map<String, Object> data = (Map<String, Object>) allAttributes.get("data");
        this.attribute = (Map<String, Object>) data.get("member");
    }

    @Override
    public String getProvider() { return "payco"; }
    @Override
    public String getProviderId() { return attribute.get("idNo").toString(); }
    @Override
    public String getEmail() { return attribute.get("email").toString(); }
    @Override
    public String getName() { return attribute.get("name").toString(); }
}
