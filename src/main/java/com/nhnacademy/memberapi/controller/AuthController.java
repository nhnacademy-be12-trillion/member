package com.nhnacademy.memberapi.controller;

import com.nhnacademy.memberapi.dto.request.LoginRequest;
import com.nhnacademy.memberapi.dto.response.TokenResponse;
import com.nhnacademy.memberapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // 토큰을 body가 아닌 header에 설정
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION,"Bearer " + tokenResponse.accessToken())
                .header("Refresh-Token", tokenResponse.refreshToken())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Refresh-Token") String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(authService.reissue(refreshToken));
    }
}