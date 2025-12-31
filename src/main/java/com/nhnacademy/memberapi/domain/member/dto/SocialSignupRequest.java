package com.nhnacademy.memberapi.domain.member.dto;

import com.nhnacademy.memberapi.domain.address.dto.AddressCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record SocialSignupRequest(
        @NotBlank
        @Email
        String memberEmail,
        @NotBlank
        String memberName,
        @NotNull
        LocalDate memberBirth,
        @NotNull
        @Pattern(regexp = "^01(?:0|1|[2-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "연락처 형식이 올바르지 않습니다.")
        String memberContact,
        @NotBlank
        String memberOauthId,
        @NotNull
        @Valid
        AddressCreateRequest memberAddress
) {}