package com.nhnacademy.memberapi.exception;

public class GradeNotFoundException extends RuntimeException {
    public GradeNotFoundException(String message) {
        super(message);
    }
}
