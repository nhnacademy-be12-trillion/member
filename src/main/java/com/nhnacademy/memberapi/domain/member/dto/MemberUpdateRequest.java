package com.nhnacademy.memberapi.domain.member.dto;

import java.time.LocalDate;

public record MemberUpdateRequest(
        String memberContact,
        String memberName,
        LocalDate memberBirth
){}