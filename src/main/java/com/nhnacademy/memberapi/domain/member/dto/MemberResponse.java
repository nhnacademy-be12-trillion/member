package com.nhnacademy.memberapi.domain.member.dto;

import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;

import java.time.LocalDate;

public record MemberResponse(
        Long memberId,
        String memberEmail,
        String memberName,
        String memberContact,
        LocalDate memberBirth,
        MemberState memberState,
        Integer memberPoint,
        String gradeName,
        String memberOauthId
) {
    public static MemberResponse fromEntity(Member member) {
        return new MemberResponse(
                member.getMemberId(),
                member.getMemberEmail(),
                member.getMemberName(),
                member.getMemberContact(),
                member.getMemberBirth(),
                member.getMemberState(),
                member.getMemberPoint(),
                member.getGrade().getGradeName().name(),
                member.getMemberOauthId()

        );
    }
}