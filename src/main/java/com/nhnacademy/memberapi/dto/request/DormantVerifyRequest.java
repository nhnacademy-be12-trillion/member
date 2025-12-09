package com.nhnacademy.memberapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DormantVerifyRequest(
        @NotBlank
        @Email
        String memberEmail,
        @NotBlank
        String verificationCode
) {
}
