package com.nhnacademy.memberapi.exception;

public class OAuthEmailNotFoundException extends RuntimeException {
    public OAuthEmailNotFoundException(String message) {
        super(message);
    }
}
