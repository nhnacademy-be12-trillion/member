package com.nhnacademy.memberapi.domain.auth.service;

import com.nhnacademy.memberapi.domain.auth.dto.CustomUserDetails;
import com.nhnacademy.memberapi.domain.auth.dto.LoginRequest;
import com.nhnacademy.memberapi.domain.auth.dto.TokenResponse;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.domain.auth.entity.RefreshToken;
import com.nhnacademy.memberapi.global.error.exception.InvalidRefreshTokenException;
import com.nhnacademy.memberapi.global.error.exception.MemberStateConflictException;
import com.nhnacademy.memberapi.domain.auth.jwt.JWTUtil;
import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import com.nhnacademy.memberapi.domain.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

    // 로그인
    public TokenResponse login(LoginRequest request) {
        // 인증 수행. Spring Security가 DTO의 memberEmail을 'Username'으로 사용하여 인증을 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.memberEmail(), request.memberPassword()));

        // 인증 성공 후 CustomUserDetails에서 memberId(PK) 추출 <- JWT 생성 시 memberEmail이 아니라 memberId를 사용하기 위해서
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Member member = memberRepository.findById(userDetails.getMemberId())
                .orElseThrow(() -> new UsernameNotFoundException("Member not found"));

        // 로그인 시간 갱신 전 방어 코드
        if (member.getMemberState() == MemberState.DORMANT) {
            throw new MemberStateConflictException("휴면 계정입니다.", MemberState.DORMANT);
        }

        // 로그인 시간 갱신
        member.setMemberLatestLoginAt(java.time.LocalDate.now());

        String role = authentication.getAuthorities().iterator().next().getAuthority();

        memberRepository.save(member);

        // 토큰 발급 및 Redis 저장
        return generateTokens(userDetails.getMemberId(), role);
    }

    // 재발급 로직
    public TokenResponse reissue(String refreshToken) {
        if (refreshToken == null) {
            throw new InvalidRefreshTokenException("Refresh token is null");
        }

        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("refresh")) {
            throw new InvalidRefreshTokenException("Invalid token category");
        }

        // Redis에서 토큰 조회 (없으면 예외 발생)
        RefreshToken storedToken = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        Long memberId = storedToken.getMemberId();
        String role = storedToken.getRole(); // Redis에 저장된 Role 사용

        // 기존 토큰 삭제 (Refresh Token Rotation)
        refreshTokenRepository.deleteById(refreshToken);

        // 새 토큰 발급 및 Redis 저장
        return generateTokens(memberId, role);
    }

    // 로그아웃
    public void logout(String refreshToken, String accessToken) {
        // 로그아웃 시 Refresh Token 삭제 (Redis)
        if (refreshToken != null && refreshTokenRepository.existsById(refreshToken)) {
            refreshTokenRepository.deleteById(refreshToken);
        }

        // Access Token 블랙리스트 처리
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            String token = accessToken.substring(7);

            // 남은 유효시간 계산
            long expiration = jwtUtil.getExpiration(token);
            long now = new Date().getTime();
            long remainTime = expiration - now;

            if (remainTime > 0) {
                redisTemplate.opsForValue()
                        .set("BL:" + token, "logout", remainTime, TimeUnit.MILLISECONDS);
            }
        }
    }

    // 토큰 생성 및 Redis 저장 공통화
    private TokenResponse generateTokens(Long memberId, String role) {
        // ms
        long accessExpire = 1800000L;      // 30분
        long refreshExpire = 86400000L;   // 24시간

        String accessToken = jwtUtil.createJwt(memberId, "access", role, accessExpire);
        String refreshToken = jwtUtil.createJwt(memberId, "refresh", role, refreshExpire);

        // Redis에 저장 (memberId, role 포함)
        refreshTokenRepository.save(new RefreshToken(refreshToken, memberId, role));

        return new TokenResponse(accessToken, refreshToken);
    }
}