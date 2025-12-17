package com.nhnacademy.memberapi.global.error.exception;

import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import lombok.Getter;


@Getter
public class MemberStateConflictException extends RuntimeException {
    private final MemberState state;

    public MemberStateConflictException(String msg, MemberState state) {
        super(msg);
        this.state = state;
    }
}
