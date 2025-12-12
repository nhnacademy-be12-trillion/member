package com.nhnacademy.memberapi.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.memberapi.domain.auth.dto.LoginRequest;
import com.nhnacademy.memberapi.domain.auth.dto.TokenResponse;
import com.nhnacademy.memberapi.domain.auth.jwt.JWTUtil;
import com.nhnacademy.memberapi.domain.auth.service.AuthService;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.global.config.SecurityConfig;
import com.nhnacademy.memberapi.global.error.exception.MemberStateConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class // OAuth 관련도 끄기
        },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class) // 2. 내 SecurityConfig도 읽지 마
        }
)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    private AuthService authService;

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



    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("test@gmail.com", "password");
        TokenResponse mockTokens = new TokenResponse("Access-Token", "Refresh-Token");

        given(authService.login(any(LoginRequest.class))).willReturn(mockTokens);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                // 헤더에 토큰이 들어갔는지 검증
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer Access-Token"))
                .andExpect(header().string("Refresh-Token", "Refresh-Token"));
    }

    @Test
    @DisplayName("로그인 실패 (비밀번호 불일치)")
    void login_Fail_BadCredentials() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("test@test.com", "wrong-pw");

        given(authService.login(any(LoginRequest.class)))
                .willThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()) // 401 확인
                .andExpect(content().string("아이디 또는 비밀번호가 일치하지 않습니다.")); // Body 메시지 확인
    }

    @Test
    @DisplayName("로그인 실패 (휴면 계정)")
    void login() throws Exception {
        LoginRequest request = new LoginRequest("dormant@test.com", "password");

        given(authService.login(any(LoginRequest.class)))
                .willThrow(new MemberStateConflictException("휴면 계정입니다.", MemberState.DORMANT));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("DORMANT_ACCOUNT"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {
        String accessToken = "Bearer AccessToken";
        String refreshToken = "Refresh-Token";
        String rawToken = "AccessToken";

        // 토큰 만료 여부 검사 통과
        given(jwtUtil.isExpired(rawToken)).willReturn(false);
        // 토큰 카테고리(access) 확인 통과
        given(jwtUtil.getCategory(rawToken)).willReturn("access");
        // Role 추출 시 null이 아닌 유효한 값 반환 (NPE 방지 핵심!)
        given(jwtUtil.getRole(rawToken)).willReturn("MEMBER");

        doNothing().when(authService).logout(any(), any());

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .header("Refresh-Token", refreshToken))
                .andExpect(status().isOk());

        // 서비스가 호출되었는지 확인
        verify(authService).logout(refreshToken, accessToken);
    }

    @Test
    @DisplayName("리프레시 토큰 재발급")
    void reissue() throws Exception {
        String refreshToken = "Refresh-Token";
        TokenResponse newTokens = new TokenResponse("newAccess-Token", "newRefresh-Token");

        given(authService.reissue(refreshToken)).willReturn(newTokens);

        mockMvc.perform(post("/api/auth/reissue")
                        .with(csrf())
                        .header("Refresh-Token", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccess-Token"))
                .andExpect(jsonPath("$.refreshToken").value("newRefresh-Token"));
    }
}