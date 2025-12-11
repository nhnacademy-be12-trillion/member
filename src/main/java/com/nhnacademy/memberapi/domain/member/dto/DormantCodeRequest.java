package com.nhnacademy.memberapi.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DormantCodeRequest(
        @NotBlank
        @Email
        String memberEmail,
        @NotBlank
        String doorayHookUrl
) {
}
