package com.nhnacademy.memberapi.domain.member.controller;

import com.nhnacademy.memberapi.domain.member.dto.*;
import com.nhnacademy.memberapi.domain.member.service.EmailService;
import com.nhnacademy.memberapi.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
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
            @RequestHeader("X-Member-Id") Long memberId
    ){
        MemberResponse response = memberService.getMember(memberId);
        return ResponseEntity.ok(response);
    }

    // 회원 수정
    @PutMapping
    public ResponseEntity<Void> updateMember(
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody MemberUpdateRequest request
    ){
        memberService.updateMember(memberId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 회원 탈퇴
    @PutMapping("/withdraw")
    public ResponseEntity<Void> withdrawMember(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken
    ) {
        memberService.withdrawMember(memberId);
        log.info("회원 탈퇴 완료: MemberEmail {}", memberId);
        return ResponseEntity.ok().build();
    }

    // 인증번호 검증
    @PostMapping("/emails/verify")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request){
        boolean isVerified = emailService.verifyCode(request.memberEmail(),  request.
                verificationCode());
        if(isVerified){
            return ResponseEntity.status(HttpStatus.OK).build();
        }else {
            log.warn("이메일 인증 실패");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 회원가입 전 인증번호 발송 API
    @PostMapping("/emails/signup")
    public ResponseEntity<Void> sendSignupEmail(@Valid @RequestBody EmailRequest request) {
        memberService.sendSignupVerificationCode(request.memberEmail());
        return ResponseEntity.ok().build();
    }

    // 비밀번호 재설정 전 인증번호 발송 API
    @PostMapping("/emails/password")
    public ResponseEntity<Void> sendResetPasswordEmail(@Valid @RequestBody EmailRequest request) {
        memberService.sendResetPasswordVerificationCode(request.memberEmail());
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

    // 휴면 아이디 인증 코드 요청
    @PostMapping("/dormant/request")
    public ResponseEntity<Void> requestDormantCode(@Valid @RequestBody DormantCodeRequest request) {
        // 이메일과 두레이 훅 URL을 받아 서비스 호출
        memberService.requestDormantRelease(request.memberEmail(), request.doorayHookUrl());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 휴면 해제 인증번호 검증 및 상태 변경
    @PostMapping("/dormant/verify")
    public ResponseEntity<Void> verifyDormantCode(@Valid @RequestBody DormantVerifyRequest request) {
        // 이메일과 사용자가 입력한 코드를 받아 검증
        memberService.processDormantRelease(request.memberEmail(), request.verificationCode());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}