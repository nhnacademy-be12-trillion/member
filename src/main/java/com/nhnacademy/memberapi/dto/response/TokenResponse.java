package com.nhnacademy.memberapi.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}
