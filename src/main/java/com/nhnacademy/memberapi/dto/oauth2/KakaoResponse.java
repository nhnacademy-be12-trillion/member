package com.nhnacademy.memberapi.dto.oauth2;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {
    private final Map<String, Object> attribute;

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvider() { return "kakao"; }
    @Override
    public String getProviderId() { return attribute.get("sub").toString(); }
    @Override
    public String getEmail() { return attribute.get("email").toString(); }
    @Override
    public String getName() { return attribute.get("name").toString(); }
}