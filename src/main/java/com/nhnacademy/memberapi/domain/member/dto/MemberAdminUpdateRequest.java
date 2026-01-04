package com.nhnacademy.memberapi.domain.member.dto;

import jakarta.validation.constraints.NotNull;

public record MemberAdminUpdateRequest(
        @NotNull Long memberId,
        @NotNull String memberState,
        @NotNull String gradeName
) {}