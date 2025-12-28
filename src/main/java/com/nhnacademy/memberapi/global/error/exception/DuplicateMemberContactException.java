package com.nhnacademy.memberapi.global.error.exception;

public class DuplicateMemberContactException extends RuntimeException {
    public DuplicateMemberContactException(String message) {
        super(message);
    }
}
