package com.nhnacademy.memberapi.domain.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.memberapi.domain.point.dto.PointPolicyUpdateRequest;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicyType;
import com.nhnacademy.memberapi.domain.point.service.PointPolicyService;
import com.nhnacademy.memberapi.global.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PointPolicyController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class
        },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
        }
)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
})
@WithMockUser(roles = "ADMIN")
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
        mockMvc.perform(get("/api/members/admin/points/policies"))
                .andExpect(status().isOk());

        verify(pointPolicyService).getPolicies();
    }

    @Test
    @DisplayName("정책 수정")
    void updatePolicyValue() throws Exception {
        PointPolicyUpdateRequest request = new PointPolicyUpdateRequest(
                "New Policy", PointPolicyType.RATE, BigDecimal.valueOf(0.05), null
        );

        mockMvc.perform(put("/api/members/admin/points/policies/{policyId}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(pointPolicyService).updatePolicy(eq(1L), any(PointPolicyUpdateRequest.class));
    }
}