package com.nhnacademy.memberapi.controller;

import com.nhnacademy.memberapi.dto.CustomUserDetails;
import com.nhnacademy.memberapi.dto.request.EmailRequest;
import com.nhnacademy.memberapi.dto.request.MemberSignupRequest;
import com.nhnacademy.memberapi.dto.request.MemberUpdateRequest;
import com.nhnacademy.memberapi.dto.request.SocialSignupRequest;
import com.nhnacademy.memberapi.dto.response.MemberResponse;
import com.nhnacademy.memberapi.dto.response.VerifyEmail;
import com.nhnacademy.memberapi.service.EmailService;
import com.nhnacademy.memberapi.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody MemberSignupRequest request) {
        memberService.signupMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //소셜 회원 가입
    @PostMapping("/signup/social")
    public ResponseEntity<Void> socialSignup(@Valid @RequestBody SocialSignupRequest request) {
        memberService.socialSignupMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //회원 조회
    @GetMapping
    public ResponseEntity<MemberResponse> getMember(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        MemberResponse response = memberService.getMember(userDetails.getMemberId());
        return ResponseEntity.ok(response);
    }

    // 회원 탈퇴
    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken
    ) {
        memberService.withdrawMember(userDetails.getMemberId(), refreshToken);
        log.info("회원 탈퇴 완료: MemberEmail {}", userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }

    // 회원 수정
    @PutMapping
    public ResponseEntity<Void> updateMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MemberUpdateRequest request
    ){
        memberService.updateMember(userDetails.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 일반 회원가입 인증번호 발송
    @PostMapping("/emails")
    public ResponseEntity<Void> sendEmail(@Valid @RequestBody EmailRequest request){
        emailService.sendVerificationCode(request.email());
        log.debug("인증번호 전송 완료 ({})", request.email());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 일반 회원가입 인증번호 검증
    @PostMapping("/emails/verify")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmail request){
        boolean isVerified = emailService.verifyCode(request.email(),  request.code());
        if(isVerified){
            return ResponseEntity.status(HttpStatus.OK).build();
        }else {
            log.warn("이메일 인증 실패");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}