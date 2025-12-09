package com.nhnacademy.memberapi.exception;

import com.nhnacademy.memberapi.entity.MemberState;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;


@Getter
public class MemberStateConflictException extends AuthenticationException {
    private final MemberState state;

    public MemberStateConflictException(String msg, MemberState state) {
        super(msg);
        this.state = state;
    }
}
