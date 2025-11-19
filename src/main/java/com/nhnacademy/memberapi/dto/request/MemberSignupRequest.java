package com.nhnacademy.memberapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record MemberSignupRequest(
        @NotBlank
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String memberEmail,
        @NotBlank
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String memberPassword,
        @NotBlank
        String memberName,
        @NotBlank
        String memberContact,
        @NotNull
        LocalDate memberBirth // YYYY-MM-DD
) {}