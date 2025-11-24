package com.nhnacademy.memberapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SocialSignupRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String name,
        @NotNull
        LocalDate birthDate,
        String contact
) {}