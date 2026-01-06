package com.nhnacademy.memberapi.domain.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.memberapi.domain.point.dto.PointHistoryResponse;
import com.nhnacademy.memberapi.domain.point.dto.PointUseRequest;
import com.nhnacademy.memberapi.domain.point.dto.ReviewPointRequest;
import com.nhnacademy.memberapi.domain.point.service.PointHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PointController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
})
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PointHistoryService pointHistoryService;

    private static final String BASE_URL = "/members/points";
    private static final String MEMBER_ID_HEADER = "X-Member-Id";
    private static final Long TEST_MEMBER_ID = 1L;

    @Test
    @DisplayName("포인트 내역 조회")
    void getPointHistories() throws Exception {
        PointHistoryResponse response = new PointHistoryResponse(
                1L,
                "회원가입 축하",
                5000,
                5000,
                LocalDateTime.now()
        );

        when(pointHistoryService.getHistories(TEST_MEMBER_ID)).thenReturn(List.of(response));

        mockMvc.perform(get(BASE_URL + "/histories")
                        .header(MEMBER_ID_HEADER, TEST_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pointHistoryId").value(1L))
                .andExpect(jsonPath("$[0].reason").value("회원가입 축하"))
                .andExpect(jsonPath("$[0].amount").value(5000))
                .andExpect(jsonPath("$[0].currentBalance").value(5000));
    }

    @Test
    @DisplayName("회원 가입 적립")
    void awardSignupPoints() throws Exception {
        doNothing().when(pointHistoryService).awardSignupPoints(TEST_MEMBER_ID);

        mockMvc.perform(post(BASE_URL + "/signup")
                        .header(MEMBER_ID_HEADER, TEST_MEMBER_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("리뷰 작성 포인트 적립")
    void awardReviewPoints() throws Exception {
        ReviewPointRequest request = new ReviewPointRequest(50L, true);

        doNothing().when(pointHistoryService).awardReviewPoints(eq(TEST_MEMBER_ID), any(ReviewPointRequest.class));

        mockMvc.perform(post(BASE_URL + "/review")
                        .header(MEMBER_ID_HEADER, TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("포인트 사용")
    void usePoints() throws Exception {
        PointUseRequest request = new PointUseRequest(200L, 3000);

        doNothing().when(pointHistoryService).usePoints(eq(TEST_MEMBER_ID), eq(200L), eq(3000));

        mockMvc.perform(post(BASE_URL + "/use")
                        .header(MEMBER_ID_HEADER, TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}