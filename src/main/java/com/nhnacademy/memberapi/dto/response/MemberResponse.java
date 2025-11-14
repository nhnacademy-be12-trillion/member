package com.nhnacademy.memberapi.dto.response;

import com.nhnacademy.memberapi.entity.Grade;
import com.nhnacademy.memberapi.entity.Member;
import com.nhnacademy.memberapi.entity.MemberState;

import java.time.LocalDate;

public record MemberResponse(
        Long memberId,
        String memberEmail,
        String memberName,
        String memberContact,
        LocalDate memberBirth,
        MemberState memberState,
        Integer memberPoint,
        Grade memberGrade
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
                member.getGrade()
        );
    }
}