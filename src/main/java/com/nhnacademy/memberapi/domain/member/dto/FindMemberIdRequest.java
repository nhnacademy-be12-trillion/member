package com.nhnacademy.memberapi.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

public record FindMemberIdRequest(
        @NotBlank
        String memberName,
        @NotBlank
        String memberContact
) {
}
