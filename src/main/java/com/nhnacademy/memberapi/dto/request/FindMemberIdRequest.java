package com.nhnacademy.memberapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FindMemberIdRequest(
        @NotBlank
        String memberName,
        @NotBlank
        String memberContact
) {
}
