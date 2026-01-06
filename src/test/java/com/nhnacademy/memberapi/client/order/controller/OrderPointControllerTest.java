package com.nhnacademy.memberapi.client.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.memberapi.client.order.dto.PointAccumulateRequest;
import com.nhnacademy.memberapi.client.order.dto.PointUsageRequest;
import com.nhnacademy.memberapi.client.order.service.OrderPointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderPointController.class) // 컨트롤러 명시
@AutoConfigureMockMvc(addFilters = false) // Security 필터 비활성화
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
})
class OrderPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderPointService orderPointService;

    private final UUID sagaId = UUID.randomUUID();
    private final String SAGA_HEADER = "X-Saga-Id";

    @Test
    @DisplayName("포인트 사용 API")
    void usePoint() throws Exception {
        PointUsageRequest request = new PointUsageRequest(1L, 100L, 5000);

        doNothing().when(orderPointService).usePoint(any(UUID.class), any(PointUsageRequest.class));

        mockMvc.perform(patch("/members/points/use")
                        .header(SAGA_HEADER, sagaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(orderPointService).usePoint(eq(sagaId), any(PointUsageRequest.class));
    }

    @Test
    @DisplayName("포인트 환불(증가) API")
    void increasePoint() throws Exception {
        PointUsageRequest request = new PointUsageRequest(1L, 100L, 5000);

        doNothing().when(orderPointService).refundPoint(any(UUID.class), any(PointUsageRequest.class));

        mockMvc.perform(patch("/members/points/increase")
                        .header(SAGA_HEADER, sagaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(orderPointService).refundPoint(eq(sagaId), any(PointUsageRequest.class));
    }

    @Test
    @DisplayName("포인트 롤백 API")
    void rollbackPoint() throws Exception {
        PointUsageRequest request = new PointUsageRequest(1L, 100L, 5000);

        doNothing().when(orderPointService).rollbackPoint(any(UUID.class), any(PointUsageRequest.class));

        mockMvc.perform(patch("/members/points/rollback")
                        .header(SAGA_HEADER, sagaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(orderPointService).rollbackPoint(eq(sagaId), any(PointUsageRequest.class));
    }

    @Test
    @DisplayName("포인트 적립 API")
    void accumulatePoint() throws Exception {
        PointAccumulateRequest request = new PointAccumulateRequest(1L, 100L, 10000);

        doNothing().when(orderPointService).accumulatePoint(any(UUID.class), any(PointAccumulateRequest.class));

        mockMvc.perform(patch("/members/points/accumulate")
                        .header(SAGA_HEADER, sagaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(orderPointService).accumulatePoint(eq(sagaId), any(PointAccumulateRequest.class));
    }
}