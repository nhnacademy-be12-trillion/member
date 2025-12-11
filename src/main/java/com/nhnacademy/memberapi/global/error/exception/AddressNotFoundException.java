package com.nhnacademy.memberapi.global.error.exception;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(String message) {
        super(message);
    }
}
