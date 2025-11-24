package com.nhnacademy.memberapi.controller;

import com.nhnacademy.memberapi.dto.CustomUserDetails;
import com.nhnacademy.memberapi.dto.request.MemberSignupRequest;
import com.nhnacademy.memberapi.dto.request.MemberUpdateRequest;
import com.nhnacademy.memberapi.dto.request.SocialSignupRequest;
import com.nhnacademy.memberapi.dto.response.MemberResponse;
import com.nhnacademy.memberapi.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

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
}