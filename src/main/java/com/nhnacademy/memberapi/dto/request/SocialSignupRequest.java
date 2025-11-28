package com.nhnacademy.memberapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record SocialSignupRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String name,
        @NotNull
        LocalDate birthDate,
        @NotNull
        @Pattern(regexp = "^01(?:0|1|[2-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "연락처 형식이 올바르지 않습니다.")
        String contact,
        @NotBlank
        String memberOauthId,

        @NotNull
        @Valid
        AddressCreateRequest address
) {}