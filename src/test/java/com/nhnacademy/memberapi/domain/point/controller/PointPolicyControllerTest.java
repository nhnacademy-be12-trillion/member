package com.nhnacademy.memberapi.domain.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.memberapi.domain.point.dto.PointPolicyUpdateRequest;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicyType;
import com.nhnacademy.memberapi.domain.point.service.PointPolicyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PointPolicyController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
})
class PointPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PointPolicyService pointPolicyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("전체 정책 조회")
    void getPolicies() throws Exception {
        mockMvc.perform(get("/members/admin/points/policies"))
                .andExpect(status().isOk());

        verify(pointPolicyService).getPolicies();
    }

    @Test
    @DisplayName("단건 정책 조회")
    void getPolicy() throws Exception {
        Long policyId = 1L;
        mockMvc.perform(get("/members/admin/points/policies/{policyId}", policyId))
                .andExpect(status().isOk());

        verify(pointPolicyService).getPolicy(policyId);
    }

    @Test
    @DisplayName("정책 수정")
    void updatePolicyValue() throws Exception {
        Long policyId = 1L;
        PointPolicyUpdateRequest request = new PointPolicyUpdateRequest(
                "PolicyUpdate", PointPolicyType.RATE, BigDecimal.valueOf(0.05), null
        );

        mockMvc.perform(put("/members/admin/points/policies/{policyId}", policyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(pointPolicyService).updatePolicy(eq(policyId), any(PointPolicyUpdateRequest.class));
    }
}