package com.nhnacademy.memberapi.domain.auth.jwt;

import com.nhnacademy.memberapi.domain.auth.dto.CustomUserDetails;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Gateway가 넣어준 헤더 확인
        String userIdStr = request.getHeader("X-Member-Id");
        String userRoleStr = request.getHeader("X-Member-Role");

        // 헤더가 없으면 다음 필터로 통과
        if (userIdStr == null || userIdStr.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Long memberId = Long.parseLong(userIdStr);
            // Role이 없으면 기본값 MEMBER 설정
            MemberRole role = (userRoleStr != null) ? MemberRole.valueOf(userRoleStr) : MemberRole.MEMBER;

            // 인증 객체 생성 (DB 조회 없이 메모리에서 바로 생성)
            Member member = Member.createForAuthentication(memberId, role);
            CustomUserDetails customUserDetails = new CustomUserDetails(member);

            // SecurityContext에 등록
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    null,
                    customUserDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            log.error("Gateway Header Authentication Failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}