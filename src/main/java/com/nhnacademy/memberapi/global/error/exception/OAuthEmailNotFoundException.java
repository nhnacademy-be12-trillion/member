package com.nhnacademy.memberapi.global.error.exception;

public class OAuthEmailNotFoundException extends RuntimeException {
    public OAuthEmailNotFoundException(String message) {
        super(message);
    }
}
