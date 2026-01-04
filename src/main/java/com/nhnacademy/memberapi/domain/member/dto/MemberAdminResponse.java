package com.nhnacademy.memberapi.domain.member.dto;

import com.nhnacademy.memberapi.domain.member.entity.Member;

import java.time.LocalDate;

public record MemberAdminResponse(
        Long memberId,
        String memberEmail,
        String memberName,
        String memberContact,
        String gradeName,
        String memberRole,
        String memberState,
        LocalDate memberLatestLoginAt
) {
    public static MemberAdminResponse fromEntity(Member member) {
        return new MemberAdminResponse(
                member.getMemberId(),
                member.getMemberEmail(),
                member.getMemberName(),
                member.getMemberContact(),
                member.getGrade().getGradeName().name(),
                member.getMemberRole().name(),
                member.getMemberState().name(),
                member.getMemberLatestLoginAt()
        );
    }
}