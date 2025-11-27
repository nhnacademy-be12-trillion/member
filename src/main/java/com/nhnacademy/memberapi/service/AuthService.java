package com.nhnacademy.memberapi.service;

import com.nhnacademy.memberapi.dto.CustomUserDetails;
import com.nhnacademy.memberapi.dto.request.LoginRequest;
import com.nhnacademy.memberapi.dto.response.TokenResponse;
import com.nhnacademy.memberapi.entity.Member;
import com.nhnacademy.memberapi.entity.MemberState;
import com.nhnacademy.memberapi.entity.RefreshToken;
import com.nhnacademy.memberapi.exception.InvalidRefreshTokenException;
import com.nhnacademy.memberapi.exception.MemberStateConflictException;
import com.nhnacademy.memberapi.jwt.JWTUtil;
import com.nhnacademy.memberapi.repository.MemberRepository;
import com.nhnacademy.memberapi.repository.RefreshTokenRepository;
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

        // 탈퇴/휴면 계정 로그인 방지 로직
        if (member.getMemberState() == MemberState.WITHDRAWAL) {
            throw new MemberStateConflictException("탈퇴한 회원입니다.");
        }
        if (member.getMemberState() == MemberState.DORMANT) {
            throw new MemberStateConflictException("휴면 계정입니다.");
        }

        // 로그인 시간 갱신
        member.setMemberLastestLoginAt(java.time.LocalDate.now());

        String role = authentication.getAuthorities().iterator().next().getAuthority();

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
        // 로그아웃 시 Refresh Token 삭제
        if (refreshToken != null && refreshTokenRepository.existsById(refreshToken)) {
            refreshTokenRepository.deleteById(refreshToken);
        }

        String token = accessToken;

        // Access Token 블랙 리스트 처리
        if(accessToken != null && accessToken.startsWith("Bearer ")) {
                token = accessToken.substring(7);
            }
        long expiration = jwtUtil.getExpiration(token);
        long now = new Date().getTime();
        long remainTime = expiration - now;

        // Access Token의 시간이 남아있으면 존재한다는 의미이므로
        if (remainTime > 0) {
            // Key: BL:토큰값, Value: logout
            redisTemplate.opsForValue()
                    .set("BL:" + token, "logout", remainTime, TimeUnit.MILLISECONDS);
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