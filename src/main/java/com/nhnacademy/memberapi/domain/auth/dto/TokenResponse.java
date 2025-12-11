package com.nhnacademy.memberapi.domain.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}
