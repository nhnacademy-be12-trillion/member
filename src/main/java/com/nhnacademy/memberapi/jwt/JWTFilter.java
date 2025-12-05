package com.nhnacademy.memberapi.jwt;

import com.nhnacademy.memberapi.dto.CustomUserDetails;
import com.nhnacademy.memberapi.entity.Member;
import com.nhnacademy.memberapi.entity.MemberRole;
import io.jsonwebtoken.JwtException; // (1) JwtException 임포트
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// OncePerRequestFilter를 상속받아 HTTP 요청당 한 번만 실행
// 인증이 필요한 모든 요청에 대해 Access Token의 유효성을 검사하고 인증 정보를 SecurityContext에 설정하는 역할
// 요청이 이 필터를 통과한 시점부터 Spring Security는 이 요청을 인증된 사용자의 요청으로 인식하며, 이후 인가(Authorization) 단계에서 설정된 권한(role)을 기반으로 리소스 접근 여부를 판단
@Slf4j
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 토큰 검증이 불필요한 경로는 토큰 검증을 건너뛰고 즉시 filterChain.doFilter()를 호출하여 다음 필터로 요청을 넘긴다.
        // signup, login, reissue, logout은 Access Token 검증이 필요 없으므로 패스
        if (requestURI.equals("/api/members/signup") ||
                requestURI.equals("/api/auth/login") ||
                requestURI.equals("/api/auth/reissue") ||
                requestURI.equals("/") ||
                requestURI.startsWith("/h2-console")) {

            filterChain.doFilter(request, response);
            return;
        }
        // HTTP 요청 헤더에서 Authorization 값을 추출 (Bearer <token> 형식)
        String authorization = request.getHeader("Authorization");
        // 토큰이 없는 요청 (정상적인 "인증 안 된" 요청) 패스
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // Bearer 접두어를 제거하고 실제 JWT 값만 추출
        String token = authorization.split(" ")[1];

        // 블랙리스트에 등록된 토큰인지 확인
        // Redis에 "BL:" + token 키가 있으면 로그아웃된 토큰으로 간주
        if (redisTemplate.hasKey("BL:" + token)) {
            log.warn("로그아웃된 토큰(Blacklist)으로 접근 시도");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            // 만료된 토큰인지 검사
            if (jwtUtil.isExpired(token)) {
                log.info("token expired");
                // 만료되었어도, 인증 안 된 상태로 다음 필터로 보냄
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰이 유효한 경우 JWTUtil을 사용하여 Payload에서 사용자 인증 정보 추출
            Long memberId = jwtUtil.getMemberId(token);
            MemberRole role = MemberRole.valueOf(jwtUtil.getRole(token));

            // 추출된 정보로 임시 Member 객체를 생성하고 이를 기반으로 CustomerDetails 객체 생성
            Member member = Member.createForAuthentication(memberId, role);
            CustomUserDetails customUserDetails = new CustomUserDetails(member);

            // 이 CustomUserDetails를 주체로 하는 UsernamePasswordAuthenticationToken 객체를 생성하여 SecurityContextHolder에 설정.
            Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // 인증 성공 상태로 다음 필터로 보냄
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            // 토큰이 잘못된 경우 (서명 오류, 형식 오류 등)
            log.error("Invalid JWT Token: {}", e.getMessage());
            // 인증 안 된 상태로 다음 필터로 보냄
            filterChain.doFilter(request, response);
        }
    }
}