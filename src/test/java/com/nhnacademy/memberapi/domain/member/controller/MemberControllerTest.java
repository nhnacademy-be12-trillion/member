package com.nhnacademy.memberapi.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.memberapi.domain.address.dto.AddressCreateRequest;
import com.nhnacademy.memberapi.domain.grade.entity.GradeName;
import com.nhnacademy.memberapi.domain.member.dto.*;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.domain.member.service.EmailService;
import com.nhnacademy.memberapi.domain.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
})
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private EmailService emailService;

    private static final String BASE_URL = "/members";
    private static final String MEMBER_ID_HEADER = "X-Member-Id";
    private static final String MEMBER_ROLE_HEADER = "X-Member-Role";
    private static final Long TEST_MEMBER_ID = 1L;

    @Test
    @DisplayName("회원 가입")
    void signup() throws Exception {
        AddressCreateRequest addressRequest = new AddressCreateRequest("12345", "도로명", "상세", "별칭");
        MemberSignupRequest request = new MemberSignupRequest(
                "test@nhn.com", "123456", "password1234!", "테스터", "010-1234-5678", LocalDate.now(), addressRequest
        );

        doNothing().when(memberService).signupMember(any(MemberSignupRequest.class));

        mockMvc.perform(post(BASE_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("소셜 회원 가입")
    void socialSignup() throws Exception {
        AddressCreateRequest addressRequest = new AddressCreateRequest("12345", "도로명", "상세", "별칭");
        SocialSignupRequest request = new SocialSignupRequest(
                "social@nhn.com",
                "테스터",
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                "oauth123",
                addressRequest
        );

        doNothing().when(memberService).socialSignupMember(any(SocialSignupRequest.class));

        mockMvc.perform(post(BASE_URL + "/signup/social")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("소셜 정보 수정")
    void updateSocialMember() throws Exception {
        AddressCreateRequest addressRequest = new AddressCreateRequest("12345", "도로명", "상세", "별칭");
        SocialSignupRequest request = new SocialSignupRequest(
                "social@nhn.com",
                "테스터",
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                "oauth123",
                addressRequest
        );

        doNothing().when(memberService).updateSocialInfo(eq(TEST_MEMBER_ID), any(SocialSignupRequest.class));

        mockMvc.perform(put(BASE_URL + "/social-info")
                        .header(MEMBER_ID_HEADER, TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원 조회")
    void getMember() throws Exception {
        // DTO 필드명이 JSON 키가 됨
        MemberResponse response = new MemberResponse(
                TEST_MEMBER_ID, "test@nhn.com", "테스터", "010-1234-5678",
                LocalDate.of(1990, 1, 1), null, 0, GradeName.COMMON.name(), null
        );

        when(memberService.getMember(TEST_MEMBER_ID)).thenReturn(response);

        mockMvc.perform(get(BASE_URL)
                        .header(MEMBER_ID_HEADER, TEST_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(TEST_MEMBER_ID))
                // 수정: email -> memberEmail, grade -> gradeName
                .andExpect(jsonPath("$.memberEmail").value("test@nhn.com"))
                .andExpect(jsonPath("$.gradeName").value(GradeName.COMMON.name()));
    }

    @Test
    @DisplayName("소셜 로그인 회원 조회 (OAuth ID)")
    void getMemberByOauthId() throws Exception {
        String oauthId = "oauth123";
        MemberResponse response = new MemberResponse(
                TEST_MEMBER_ID, "social@nhn.com", "테스터", null,
                LocalDate.now(), null, 0, GradeName.COMMON.name(), null
        );

        when(memberService.getMemberByOauthId(oauthId)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/social/{oauthId}", oauthId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberEmail").value("social@nhn.com"))
                .andExpect(jsonPath("$.gradeName").value(GradeName.COMMON.name()));
    }

    @Test
    @DisplayName("회원 정보 수정")
    void updateMember() throws Exception {
        MemberUpdateRequest request = new MemberUpdateRequest(
                "010-9999-9999", "새이름", LocalDate.now()
        );

        doNothing().when(memberService).updateMember(eq(TEST_MEMBER_ID), any(MemberUpdateRequest.class));

        mockMvc.perform(put(BASE_URL)
                        .header(MEMBER_ID_HEADER, TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("회원 탈퇴")
    void withdrawMember() throws Exception {
        String refreshToken = "some-refresh-token";
        doNothing().when(memberService).withdrawMember(TEST_MEMBER_ID);

        mockMvc.perform(put(BASE_URL + "/withdraw")
                        .header(MEMBER_ID_HEADER, TEST_MEMBER_ID)
                        .header("Refresh-Token", refreshToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail_Success() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest("test@nhn.com", "123456");

        when(emailService.verifyCode(request.memberEmail(), request.verificationCode())).thenReturn(true);

        mockMvc.perform(post(BASE_URL + "/emails/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 인증 실패")
    void verifyEmail_Fail() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest("test@nhn.com", "wrong");

        when(emailService.verifyCode(request.memberEmail(), request.verificationCode())).thenReturn(false);

        mockMvc.perform(post(BASE_URL + "/emails/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 인증번호 발송")
    void sendSignupEmail() throws Exception {
        EmailRequest request = new EmailRequest("test@nhn.com");

        doNothing().when(memberService).sendSignupVerificationCode(request.memberEmail());

        mockMvc.perform(post(BASE_URL + "/emails/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호 재설정 인증번호 발송")
    void sendResetPasswordEmail() throws Exception {
        EmailRequest request = new EmailRequest("test@nhn.com");

        doNothing().when(memberService).sendResetPasswordVerificationCode(request.memberEmail());

        mockMvc.perform(post(BASE_URL + "/emails/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호 재설정")
    void resetPassword() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest("test@nhn.com", "123456", "newPwd123!");

        doNothing().when(memberService).resetPassword(any(PasswordResetRequest.class));

        mockMvc.perform(put(BASE_URL + "/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("아이디(이메일) 찾기")
    void findId() throws Exception {
        FindMemberIdRequest request = new FindMemberIdRequest("테스터", "010-1234-5678");
        String maskedEmail = "te**@nhn.com";

        when(memberService.findMemberEmail(any(FindMemberIdRequest.class))).thenReturn(maskedEmail);

        mockMvc.perform(post(BASE_URL + "/findEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(maskedEmail));
    }

    @Test
    @DisplayName("휴면 해제 요청")
    void requestDormantCode() throws Exception {
        DormantCodeRequest request = new DormantCodeRequest("dormant@nhn.com", "http://hook");

        doNothing().when(memberService).requestDormantRelease(request.memberEmail(), request.doorayHookUrl());

        mockMvc.perform(post(BASE_URL + "/dormant/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("휴면 해제 검증")
    void verifyDormantCode() throws Exception {
        DormantVerifyRequest request = new DormantVerifyRequest("dormant@nhn.com", "123456");

        doNothing().when(memberService).processDormantRelease(request.memberEmail(), request.verificationCode());

        mockMvc.perform(post(BASE_URL + "/dormant/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("전체 회원 조회 (관리자)")
    void getMembersByAdmin() throws Exception {
        MemberAdminResponse adminResponse = new MemberAdminResponse(
                TEST_MEMBER_ID,
                "test@nhn.com",
                "테스터",
                "010-1234-5678",
                GradeName.COMMON.name(),
                MemberRole.MEMBER.name(),
                MemberState.ACTIVE.name(),
                LocalDate.now()
        );
        Page<MemberAdminResponse> page = new PageImpl<>(Collections.singletonList(adminResponse));

        when(memberService.getMembersByAdmin(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/admin")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].memberEmail").value("test@nhn.com"))
                .andExpect(jsonPath("$.content[0].gradeName").value(GradeName.COMMON.name()))
                .andExpect(jsonPath("$.content[0].memberRole").value(MemberRole.MEMBER.name()));
    }

    @Test
    @DisplayName("회원 상태 수정 (관리자 성공)")
    void updateMemberByAdmin_Success() throws Exception {
        MemberAdminUpdateRequest request = new MemberAdminUpdateRequest(
                TEST_MEMBER_ID,
                MemberState.ACTIVE.name(),
                GradeName.GOLD.name()
        );

        doNothing().when(memberService).updateMemberByAdmin(any(MemberAdminUpdateRequest.class));

        mockMvc.perform(put(BASE_URL + "/admin")
                        .header(MEMBER_ROLE_HEADER, MemberRole.ADMIN.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원 상태 수정 (관리자 권한 없음)")
    void updateMemberByAdmin_Fail() throws Exception {
        MemberAdminUpdateRequest request = new MemberAdminUpdateRequest(
                TEST_MEMBER_ID,
                MemberState.ACTIVE.name(),
                GradeName.GOLD.name()
        );

        mockMvc.perform(put(BASE_URL + "/admin")
                        .header(MEMBER_ROLE_HEADER, MemberRole.MEMBER.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}