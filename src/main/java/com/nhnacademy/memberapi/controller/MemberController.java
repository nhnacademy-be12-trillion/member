package com.nhnacademy.memberapi.controller;

import com.nhnacademy.memberapi.dto.CustomUserDetails;
import com.nhnacademy.memberapi.dto.request.*;
import com.nhnacademy.memberapi.dto.response.MemberResponse;
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

    // 회원 수정
    @PutMapping
    public ResponseEntity<Void> updateMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MemberUpdateRequest request
    ){
        memberService.updateMember(userDetails.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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

    // 인증번호 검증
    @PostMapping("/emails/verify")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request){
        boolean isVerified = emailService.verifyCode(request.email(),  request.code());
        if(isVerified){
            return ResponseEntity.status(HttpStatus.OK).build();
        }else {
            log.warn("이메일 인증 실패");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 회원가입 전 인증번호 발송 API
    @PostMapping("/signup/emails")
    public ResponseEntity<Void> sendSignupEmail(@Valid @RequestBody EmailRequest request) {
        memberService.sendSignupVerificationCode(request.email());
        return ResponseEntity.ok().build();
    }

    // 비밀번호 재설정 전 인증번호 발송 API
    @PostMapping("/password/emails")
    public ResponseEntity<Void> sendResetPasswordEmail(@Valid @RequestBody EmailRequest request) {
        memberService.sendResetPasswordVerificationCode(request.email());
        return ResponseEntity.ok().build();
    }

    // 본인 인증이 완료되면 비밀번호 재설정
    @PutMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request){
        memberService.resetPassword(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 전화번호와 이름으로 아이디 조회
    @PostMapping("/findEmail")
    public ResponseEntity<String> findId(@Valid @RequestBody FindMemberIdRequest request) {
        String maskedEmail = memberService.findMemberEmail(request);
        return ResponseEntity.status(HttpStatus.OK).body(maskedEmail);
    }


}