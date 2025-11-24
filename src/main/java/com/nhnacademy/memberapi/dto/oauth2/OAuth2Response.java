package com.nhnacademy.memberapi.dto.oauth2;

public interface OAuth2Response {
    String getProvider();
    String getProviderId();
    String getEmail();
    String getName();
}
