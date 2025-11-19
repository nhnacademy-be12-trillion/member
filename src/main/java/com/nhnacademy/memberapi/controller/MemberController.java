package com.nhnacademy.memberapi.controller;

import com.nhnacademy.memberapi.dto.request.MemberSignupRequest;
import com.nhnacademy.memberapi.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {

    private final MemberService memberService;

    // MemberService 주입 (LoginController에 이미 있다면 생략)
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 1. 회원가입 엔드포인트 (테스트용)
     * SecurityConfig에서 /login 외에는 모두 authenticated()이므로,
     * /signup 도 허용해줘야 합니다.
     */
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody MemberSignupRequest request) {
        memberService.signupMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 2. JWT 인증 테스트용 엔드포인트
     * (SecurityConfig에서 .anyRequest().authenticated()에 해당)
     */
    @GetMapping("/info")
    public ResponseEntity<String> getMyInfo() {
        // SecurityContext에서 인증 정보 꺼내기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName(); // CustomUserDetails의 getUsername() 반환값 (이메일)

        return ResponseEntity.ok("인증 성공! 현재 사용자: " + currentEmail);
    }

    /**
     * 3. ADMIN 권한 테스트용 엔드포인트
     * (SecurityConfig에서 .requestMatchers("/admin").hasRole("ADMIN")에 해당)
     */
    @GetMapping("/admin")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("ADMIN 전용 API 접근 성공");
    }
}