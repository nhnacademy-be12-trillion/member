package com.nhnacademy.memberapi.global.error.exception;

public class AddressMaxSizeException extends RuntimeException {
    public AddressMaxSizeException(String message) {
        super(message);
    }
}
