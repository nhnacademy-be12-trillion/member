package com.nhnacademy.memberapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
        @NotBlank
        @Email
        String memberEmail,

        // 사용자가 입력
        @NotBlank
        String verificationCode,

        @NotBlank
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String newPassword
) {}