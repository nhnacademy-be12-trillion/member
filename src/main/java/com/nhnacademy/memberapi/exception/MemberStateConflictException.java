package com.nhnacademy.memberapi.exception;

public class MemberStateConflictException extends RuntimeException {
    public MemberStateConflictException(String message) {
        super(message);
    }
}
