package com.nhnacademy.memberapi.security;

import org.springframework.security.test.context.support.WithSecurityContext;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockAuthUserSecurityContextFactory.class) // 공장 연결
public @interface WithMockAuthUser {
    String email() default "test@gmail.com";
    String role() default "MEMBER";
}