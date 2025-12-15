package com.nhnacademy.memberapi.domain.auth.jwt;

import com.nhnacademy.memberapi.domain.auth.dto.CustomUserDetails;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GatewayAuthenticationFilterTest {

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private GatewayAuthenticationFilter gatewayAuthenticationFilter;

    @AfterEach
    void tearDown() {
        // 테스트 간 SecurityContext 오염 방지
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("헤더에 X-Member-Id가 없으면 인증 없이 다음 필터로 넘어가기")
    void doFilterInternal_noHeader() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        // 헤더 설정 없음
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        gatewayAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("헤더에 X-Member-Id와 Role이 있으면 인증 객체가 생성")
    void doFilterInternal_validHeaders() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Member-Id", "123");
        request.addHeader("X-Member-Role", "ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        gatewayAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        assertThat(userDetails.getMemberId()).isEqualTo(123L);

        // 권한 확인 (ROLE_ 접두사 확인은 CustomUserDetails 구현에 따라 다를 수 있음)
        boolean hasAdminRole = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
        assertThat(hasAdminRole).isTrue();
    }

    @Test
    @DisplayName("X-Member-Role 헤더가 없으면 기본값(MEMBER)으로 설정된다")
    void doFilterInternal_defaultRole() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Member-Id", "456");
        // X-Member-Role 헤더 누락
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        gatewayAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        assertThat(userDetails.getMemberRole()).isEqualTo(MemberRole.MEMBER);
    }

    @Test
    @DisplayName("X-Member-Id 형식이 숫자가 아니면 인증 실패 후 다음 필터로 넘어간다")
    void doFilterInternal_invalidIdFormat() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Member-Id", "invalid_id"); // 숫자가 아님
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        gatewayAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // 예외가 발생해도 filterChain은 진행되어야 함
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}