package com.nhnacademy.memberapi.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmail(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String code
) {
}
