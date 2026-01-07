package com.nhnacademy.memberapi.domain.member.controller.docs;

import com.nhnacademy.memberapi.domain.member.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Member", description = "회원 관리 API")
public interface MemberControllerDocs {

    @Operation(summary = "회원 가입", description = "일반 회원 가입을 처리합니다.")
    @ApiResponse(responseCode = "201", description = "회원 가입 성공")
    ResponseEntity<Void> signup(@Valid @RequestBody MemberSignupRequest request);

    @Operation(summary = "소셜 회원 가입", description = "OAuth2 소셜 계정으로 회원 가입을 처리합니다.")
    @ApiResponse(responseCode = "201", description = "소셜 회원 가입 성공")
    ResponseEntity<Void> socialSignup(@Valid @RequestBody SocialSignupRequest request);

    @Operation(summary = "소셜 회원 추가 정보 수정", description = "소셜 회원 가입 후 추가 정보를 입력받아 수정합니다.")
    @ApiResponse(responseCode = "200", description = "정보 수정 성공")
    ResponseEntity<Void> updateSocialMember(
            @Parameter(description = "회원 ID", required = true) @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody @Valid SocialSignupRequest request
    );

    @Operation(summary = "회원 조회", description = "로그인한 회원의 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공")
    ResponseEntity<MemberResponse> getMember(
            @Parameter(description = "회원 ID", required = true) @RequestHeader("X-Member-Id") Long memberId
    );

    @Operation(summary = "소셜 회원 조회 (OAuth ID)", description = "OAuth Provider ID로 소셜 회원을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공")
    ResponseEntity<MemberResponse> getMemberByOauthId(
            @Parameter(description = "OAuth ID", required = true) @PathVariable("oauthId") String oauthId
    );

    @Operation(summary = "회원 정보 수정", description = "회원의 개인정보를 수정합니다.")
    @ApiResponse(responseCode = "204", description = "회원 정보 수정 성공")
    ResponseEntity<Void> updateMember(
            @Parameter(description = "회원 ID", required = true) @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody MemberUpdateRequest request
    );

    @Operation(summary = "회원 탈퇴", description = "회원을 탈퇴 상태로 변경합니다.")
    @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공")
    ResponseEntity<Void> withdrawMember(
            @Parameter(description = "회원 ID", required = true) @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "Refresh Token (선택)") @RequestHeader(value = "Refresh-Token", required = false) String refreshToken
    );

    @Operation(summary = "이메일 인증번호 검증", description = "이메일로 발송된 인증번호를 검증합니다.")
    @ApiResponse(responseCode = "200", description = "인증 성공")
    @ApiResponse(responseCode = "400", description = "인증 실패")
    ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request);

    @Operation(summary = "회원가입 인증메일 발송", description = "회원가입을 위한 인증 코드를 이메일로 발송합니다.")
    @ApiResponse(responseCode = "200", description = "메일 발송 성공")
    ResponseEntity<Void> sendSignupEmail(@Valid @RequestBody EmailRequest request);

    @Operation(summary = "비밀번호 재설정 인증메일 발송", description = "비밀번호 재설정을 위한 인증 코드를 이메일로 발송합니다.")
    @ApiResponse(responseCode = "200", description = "메일 발송 성공")
    ResponseEntity<Void> sendResetPasswordEmail(@Valid @RequestBody EmailRequest request);

    @Operation(summary = "비밀번호 재설정", description = "인증이 완료된 회원의 비밀번호를 재설정합니다.")
    @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공")
    ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request);

    @Operation(summary = "아이디(이메일) 찾기", description = "전화번호와 이름을 이용해 마스킹된 아이디(이메일)를 찾습니다.")
    @ApiResponse(responseCode = "200", description = "아이디 조회 성공")
    ResponseEntity<String> findId(@Valid @RequestBody FindMemberIdRequest request);

    @Operation(summary = "휴면 해제 인증 요청", description = "휴면 계정 해제를 위한 인증 코드를 요청합니다.")
    @ApiResponse(responseCode = "204", description = "인증 코드 발송 성공")
    ResponseEntity<Void> requestDormantCode(@Valid @RequestBody DormantCodeRequest request);

    @Operation(summary = "휴면 해제 인증 검증", description = "휴면 해제 인증 코드를 검증하고 상태를 변경합니다.")
    @ApiResponse(responseCode = "204", description = "휴면 해제 성공")
    ResponseEntity<Void> verifyDormantCode(@Valid @RequestBody DormantVerifyRequest request);

    @Operation(summary = "전체 회원 조회 (관리자)", description = "관리자가 전체 회원 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공")
    ResponseEntity<Page<MemberAdminResponse>> getMembersByAdmin(Pageable pageable);

    @Operation(summary = "회원 상태/등급 수정 (관리자)", description = "관리자가 회원의 상태나 등급을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    ResponseEntity<Void> updateMemberByAdmin(
            @Parameter(description = "회원 권한", required = true) @RequestHeader("X-Member-Role") String memberRole,
            @Valid @RequestBody MemberAdminUpdateRequest request
    );
}