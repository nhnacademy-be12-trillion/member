package com.nhnacademy.memberapi.controller;

import com.nhnacademy.memberapi.dto.request.LoginRequest;
import com.nhnacademy.memberapi.dto.request.MemberSignupRequest;
import com.nhnacademy.memberapi.dto.response.TokenResponse;
import com.nhnacademy.memberapi.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody MemberSignupRequest request) {
        memberService.signupMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        // 서비스 호출
        TokenResponse tokenResponse = memberService.login(loginRequest);

        // 응답 헤더 & 쿠키 설정 (HTTP 관련 처리는 컨트롤러의 몫)
        response.setHeader("Authorization", "Bearer " + tokenResponse.accessToken());
        response.addCookie(createCookie("refresh", tokenResponse.refreshToken()));

        return ResponseEntity.ok("Login Success");
    }


    // 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refresh = getRefreshTokenFromCookie(request);

        try {
            // 서비스 호출
            TokenResponse tokenResponse = memberService.reissue(refresh);

            // 응답 설정
            response.setHeader("Authorization", "Bearer " + tokenResponse.accessToken());
            response.addCookie(createCookie("refresh", tokenResponse.refreshToken()));

            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String refresh = getRefreshTokenFromCookie(request);

        // 서비스 호출
        memberService.logout(refresh);

        // 쿠키 삭제
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok("Logout Success");
    }

    // 쿠키 생성 유틸 메서드 (View/HTTP 계층 관련이므로 컨트롤러에 위치하거나 별도 Utils로 분리)
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    // 쿠키 추출 유틸 메서드
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}