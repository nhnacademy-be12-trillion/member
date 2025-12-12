package com.nhnacademy.memberapi.domain.auth.jwt;

import com.nhnacademy.memberapi.domain.auth.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTFilterTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JWTFilter jwtFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("화이트리스트 경로는 토큰 검사 없이 통과해야 한다")
    void doFilterInternal_whitelist() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(redisTemplate, never()).hasKey(any());
    }

    @Test
    @DisplayName("헤더에 토큰이 없으면 그냥 통과한다 (이후 필터에서 잡힘)")
    void doFilterInternal_noToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/members/me");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("블랙리스트(로그아웃)된 토큰이면 401 에러를 반환한다")
    void doFilterInternal_blacklist() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/members/me");
        request.addHeader("Authorization", "Bearer blackToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(redisTemplate.hasKey("BL:blackToken")).willReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("유효한 토큰이면 SecurityContext에 인증 정보를 저장한다")
    void doFilterInternal_validToken() throws ServletException, IOException {
        String validToken = "validToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/members/me");
        request.addHeader("Authorization", "Bearer " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(redisTemplate.hasKey("BL:" + validToken)).willReturn(false);
        given(jwtUtil.isExpired(validToken)).willReturn(false);
        given(jwtUtil.getMemberId(validToken)).willReturn(100L);
        given(jwtUtil.getRole(validToken)).willReturn("MEMBER");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CustomUserDetails userDetails = (CustomUserDetails) principal;

        assertThat(userDetails.getUsername()).isEqualTo("jwt@temp.com");
        assertThat(userDetails.getMemberId()).isEqualTo(100L);
    }
}