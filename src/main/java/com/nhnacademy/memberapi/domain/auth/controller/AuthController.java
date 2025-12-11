package com.nhnacademy.memberapi.domain.auth.controller;

import com.nhnacademy.memberapi.domain.auth.dto.LoginRequest;
import com.nhnacademy.memberapi.global.error.ErrorResponse;
import com.nhnacademy.memberapi.domain.auth.dto.TokenResponse;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.global.error.exception.MemberStateConflictException;
import com.nhnacademy.memberapi.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // 토큰을 body가 아닌 header에 설정
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try{
            TokenResponse tokenResponse = authService.login(request);
            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION,"Bearer " + tokenResponse.accessToken())
                    .header("Refresh-Token", tokenResponse.refreshToken())
                    .build();
        }catch (Exception ex) {
            // 진짜 예외 원인 찾기
            Throwable cause = ex;
            if (ex instanceof org.springframework.security.authentication.InternalAuthenticationServiceException) {
                cause = ex.getCause();
            }

            if (cause instanceof MemberStateConflictException e) {
                if (e.getState() == MemberState.DORMANT) {
                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(ErrorResponse.of("DORMANT_ACCOUNT", HttpStatus.FORBIDDEN.value(), e.getMessage()));
                } else {
                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(ErrorResponse.of("WITHDRAWAL_ACCOUNT", HttpStatus.FORBIDDEN.value(), e.getMessage()));
                }
            }
            if (ex instanceof BadCredentialsException) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
            }
            throw new RuntimeException(ex);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Refresh-Token", required = false) String refreshToken, @RequestHeader("Authorization") String accessToken) {
        authService.logout(refreshToken, accessToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(authService.reissue(refreshToken));
    }
}