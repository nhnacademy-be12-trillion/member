package com.nhnacademy.memberapi.jwt;

import com.nhnacademy.memberapi.dto.CustomUserDetails;
import com.nhnacademy.memberapi.entity.Member;
import com.nhnacademy.memberapi.entity.MemberRole;
import io.jsonwebtoken.JwtException; // (1) JwtException 임포트
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // [수정 1] .startsWith() 대신 .endsWith() 사용
        // 이렇게 하면 /signup, /api/signup, /api/v1/signup 등 모두 통과
        if (requestURI.endsWith("/signup") || requestURI.endsWith("/login") || requestURI.equals("/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");

        // 토큰이 없는 요청 (정상적인 "인증 안 된" 요청)
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.split(" ")[1];

        // [수정 2] try-catch 로직을 "double doFilter" 버그 없도록 수정
        try {
            // 1. 만료된 토큰인지 검사
            if (jwtUtil.isExpired(token)) {
                System.out.println("token expired");
                // 만료되었어도, "인증 안 된" 상태로 다음 필터로 보냄
                filterChain.doFilter(request, response);
                return;
            }

            // 2. 토큰이 유효한 경우 -> 인증 정보 생성
            String username = jwtUtil.getUsername(token);
            MemberRole role = MemberRole.valueOf(jwtUtil.getRole(token));

            Member member = Member.createForAuthentication(username, role);
            CustomUserDetails customUserDetails = new CustomUserDetails(member);

            Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // "인증 성공" 상태로 다음 필터로 보냄
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            // 3. 토큰이 "잘못된" 경우 (서명 오류, 형식 오류 등)
            System.out.println("Invalid JWT Token: " + e.getMessage());
            // "인증 안 된" 상태로 다음 필터로 보냄
            filterChain.doFilter(request, response);
        }

        // [수정 3] 메서드 맨 마지막의 filterChain.doFilter(request, response); 는
        // try 블록과 catch 블록 안으로 모두 이동했으므로 여기서 "반드시" 제거되어야 합니다.
    }
}