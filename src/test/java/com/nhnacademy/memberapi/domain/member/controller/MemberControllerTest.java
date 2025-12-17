package com.nhnacademy.memberapi.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.memberapi.domain.address.dto.AddressCreateRequest;
import com.nhnacademy.memberapi.domain.member.dto.*;
import com.nhnacademy.memberapi.domain.member.service.EmailService;
import com.nhnacademy.memberapi.domain.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
})
@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @org.mockito.Mock
    private MemberService memberService;

    @org.mockito.Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(new MemberController(memberService, emailService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("회원 가입")
    void signup() throws Exception {
        AddressCreateRequest addressRequest = new AddressCreateRequest(
                "12345", "도로명 주소", "상세 주소", "별칭"
        );

        MemberSignupRequest request = new MemberSignupRequest(
                "test@nhn.com",
                "123456",
                "password123!",
                "테스터",
                "010-1234-5678",
                LocalDate.of(1990, 1, 1),
                addressRequest
        );

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());

        verify(memberService).signupMember(any(MemberSignupRequest.class));
    }

    @Test
    @DisplayName("내 정보 조회 (인증된 사용자)")
    void getMember() {
        Long memberId = 1L;

        MemberResponse response = new MemberResponse(
                memberId,
                "test@nhn.com",
                "테스터",
                "010-1234-5678",
                LocalDate.of(1990, 1, 1),
                null,
                0,
                "SILVER"
        );
        given(memberService.getMember(memberId)).willReturn(response);
        verify(memberService).getMember(eq(memberId));
    }

    @Test
    @DisplayName("회원 정보 수정")
    void updateMember(){
        Long memberId = 1L;
        MemberUpdateRequest request = new MemberUpdateRequest(
                "010-9876-5432",
                "수정된이름",
                LocalDate.now()
        );
        verify(memberService).updateMember(eq(memberId), request);
    }

    @Test
    @DisplayName("회원 탈퇴 - Refresh Token 헤더 포함")
    void withdraw() {
        Long memberId = 1L;
        verify(memberService).withdrawMember(memberId);
    }

    @Test
    @DisplayName("이메일 인증 코드 검증")
    void verifyEmail() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest("test@nhn.com", "123456");
        given(emailService.verifyCode(request.email(), request.code())).willReturn(true);

        mockMvc.perform(post("/api/members/emails/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("휴면 해제 요청")
    void requestDormantCode() throws Exception {
        DormantCodeRequest request = new DormantCodeRequest("dormant@nhn.com", "https://hook.dooray.com/...");

        mockMvc.perform(post("/api/members/dormant/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(memberService).requestDormantRelease(request.memberEmail(), request.doorayHookUrl());
    }
}