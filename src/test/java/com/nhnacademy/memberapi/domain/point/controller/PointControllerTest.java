//package com.nhnacademy.memberapi.domain.point.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.nhnacademy.memberapi.domain.member.entity.Member;
//import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
//import com.nhnacademy.memberapi.domain.point.dto.BookPointRequest;
//import com.nhnacademy.memberapi.domain.point.dto.PointRefundRequest;
//import com.nhnacademy.memberapi.domain.point.dto.PointUseRequest;
//import com.nhnacademy.memberapi.domain.point.dto.ReviewPointRequest;
//import com.nhnacademy.memberapi.domain.point.service.PointHistoryService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(
//        controllers = PointController.class,
//        excludeAutoConfiguration = {
//                OAuth2ClientAutoConfiguration.class
//        }
//)
//@TestPropertySource(properties = {
//        "spring.cloud.config.enabled=false",
//        "spring.cloud.config.import-check.enabled=false",
//})
//class PointControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private PointHistoryService pointHistoryService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @BeforeEach
//    void setUp() {
//        Member member = mock(Member.class);
//
//        given(member.getMemberId()).willReturn(1L);
//        given(member.getMemberEmail()).willReturn("test@test.com");
//        given(member.getMemberPassword()).willReturn("password");
//        given(member.getMemberRole()).willReturn(MemberRole.MEMBER);
//    }
//
//    @Test
//    @DisplayName("포인트 내역 조회")
//    void getPointHistories() throws Exception {
//        mockMvc.perform(get("/api/members/points/histories")
//                .andExpect(status().isOk());
//
//        verify(pointHistoryService).getHistories(1L);
//    }
//
//    @Test
//    @DisplayName("회원가입 포인트 적립")
//    void awardSignupPoints() throws Exception {
//        mockMvc.perform(post("/api/members/points/signup")
//                .andExpect(status().isOk());
//
//        verify(pointHistoryService).awardSignupPoints(1L);
//    }
//
//    @Test
//    @DisplayName("도서 구매 포인트 적립")
//    void awardPurchasePoints() throws Exception {
//        BookPointRequest request = new BookPointRequest(100L, 50000);
//
//        mockMvc.perform(post("/api/members/points/purchase")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk());
//
//        verify(pointHistoryService).awardPurchasePoints(eq(1L), eq(100L), eq(50000));
//    }
//
//    @Test
//    @DisplayName("리뷰 포인트 적립")
//    void awardReviewPoints() throws Exception {
//        ReviewPointRequest request = new ReviewPointRequest(50L, true);
//
//        mockMvc.perform(post("/api/members/points/review")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk());
//
//        verify(pointHistoryService).awardReviewPoints(eq(1L), eq(request));
//    }
//
//    @Test
//    @DisplayName("포인트 환불")
//    void refundPoints() throws Exception {
//        PointRefundRequest request = new PointRefundRequest(1L, 100L);
//
//        mockMvc.perform(post("/api/members/points/refund")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk());
//
//        verify(pointHistoryService).refundPurchasePoints(1L, 100L);
//    }
//
//    @Test
//    @DisplayName("포인트 사용")
//    void usePoints() throws Exception {
//        PointUseRequest request = new PointUseRequest(200L, 3000);
//
//        mockMvc.perform(post("/api/members/points/use")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk());
//
//        verify(pointHistoryService).usePoints(eq(1L), eq(200L), eq(3000));
//    }
//}