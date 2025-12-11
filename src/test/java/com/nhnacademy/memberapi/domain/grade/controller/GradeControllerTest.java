package com.nhnacademy.memberapi.domain.grade.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.memberapi.domain.auth.jwt.JWTUtil;
import com.nhnacademy.memberapi.domain.grade.dto.GradeRequest;
import com.nhnacademy.memberapi.domain.grade.dto.GradeResponse;
import com.nhnacademy.memberapi.domain.grade.entity.GradeName;
import com.nhnacademy.memberapi.domain.grade.service.GradeService;
import com.nhnacademy.memberapi.global.config.SecurityConfig;
import com.nhnacademy.memberapi.global.error.exception.GradeNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = GradeController.class,
        excludeAutoConfiguration = {
                // SecurityAutoConfiguration은 HttpSecurity를 위해 켜둡니다.
                OAuth2ClientAutoConfiguration.class
        },
        excludeFilters = {
                // 프로덕션 SecurityConfig 제외
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
        }
)
@Import(GradeControllerTest.TestSecurityConfig.class) // 테스트 설정 로드
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.jwt.secret=testsecret"
})
class GradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GradeService gradeService;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    // 테스트용 시큐리티 설정 (모든 요청 허용, CSRF 끔)
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    );
            return http.build();
        }
    }

    private GradeResponse createMockResponse(Long id, GradeName name) {
        return new GradeResponse(id, name, BigDecimal.valueOf(0.01), 1000);
    }

    @Test
    @DisplayName("GET / 전체 등급 목록 조회")
    void getGrades_Success() throws Exception {
        // Given
        GradeResponse mockResponse = createMockResponse(1L, GradeName.COMMON);
        given(gradeService.getGrades()).willReturn(List.of(mockResponse));

        // When & Then
        mockMvc.perform(get("/api/members/grades")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].gradeName").value("COMMON"));
    }

    @Test
    @DisplayName("GET /{gradeId} 특정 등급 단건 조회 성공")
    void getGrade_Success() throws Exception {
        // Given
        Long gradeId = 2L;
        GradeResponse mockResponse = createMockResponse(gradeId, GradeName.ROYAL);
        given(gradeService.getGrade(gradeId)).willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/members/grades/{gradeId}", gradeId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeName").value("ROYAL"))
                .andExpect(jsonPath("$.gradeCondition").value(1000));
    }

    @Test
    @DisplayName("PUT /{gradeId} 등급 정책 수정 성공 (200 OK)")
    void updateGrade_Success() throws Exception {
        // Given
        Long gradeId = 3L;
        GradeRequest request = new GradeRequest(GradeName.GOLD, BigDecimal.valueOf(0.05), 5000);
        GradeResponse updatedResponse = new GradeResponse(gradeId, GradeName.GOLD, BigDecimal.valueOf(0.05), 5000);

        given(gradeService.updateGrade(eq(gradeId), any(GradeRequest.class))).willReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/members/grades/{gradeId}", gradeId)
                        .with(csrf()) // PUT 요청 시 CSRF 토큰 필수
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradePointRatio").value(0.05))
                .andExpect(jsonPath("$.gradeCondition").value(5000));
    }

    @Test
    @DisplayName("PUT /{gradeId} 존재하지 않는 등급 수정 시도 시 404 NOT FOUND")
    void updateGrade_Fail_NotFound() throws Exception {
        // Given
        Long gradeId = 99L;
        GradeRequest request = new GradeRequest(GradeName.GOLD, BigDecimal.valueOf(0.05), 5000);
        given(gradeService.updateGrade(anyLong(), any(GradeRequest.class)))
                .willThrow(new GradeNotFoundException("Grade not found: " + gradeId));

        // When & Then
        mockMvc.perform(put("/api/members/grades/{gradeId}", gradeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}