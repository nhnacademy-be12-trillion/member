package com.nhnacademy.memberapi.dto.request;

import java.time.LocalDate;

public record MemberUpdateRequest(
        String memberContact,
        String memberName,
        LocalDate memberBirth
){}