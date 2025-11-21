package com.nhnacademy.memberapi.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record MemberUpdateRequest(
        @NotBlank
        String memberPassword,
        @NotBlank
        String memberContact,
        @NotBlank
        String memberName,
        @NotBlank
        LocalDate memberBirth
){}