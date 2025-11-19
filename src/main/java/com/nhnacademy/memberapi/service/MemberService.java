package com.nhnacademy.memberapi.service;

import com.nhnacademy.memberapi.dto.CustomUserDetails;
import com.nhnacademy.memberapi.dto.request.LoginRequest;
import com.nhnacademy.memberapi.dto.request.MemberSignupRequest;
import com.nhnacademy.memberapi.dto.response.TokenResponse;
import com.nhnacademy.memberapi.entity.*;
import com.nhnacademy.memberapi.exception.UserAlreadyExistsException;
import com.nhnacademy.memberapi.jwt.JWTUtil;
import com.nhnacademy.memberapi.repository.GradeRepository;
import com.nhnacademy.memberapi.repository.MemberRepository;
import com.nhnacademy.memberapi.repository.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Iterator;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final GradeRepository gradeRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;

    // 회원가입
    public void signupMember(MemberSignupRequest request) {
        if (memberRepository.existsByMemberEmail(request.memberEmail())) {
            throw new UserAlreadyExistsException(request.memberEmail());
        }

        Grade defaultGrade = gradeRepository.findByGradeName(GradeName.COMMON)
                .orElseGet(() -> {
                    Grade newGrade = Grade.builder()
                            .gradeName(GradeName.COMMON)
                            .gradeCondition(0)
                            .gradePointRatio(1)
                            .build();
                    return gradeRepository.save(newGrade);
                });

        Member member = Member.builder()
                .memberPassword(passwordEncoder.encode(request.memberPassword()))
                .memberName(request.memberName())
                .memberContact(request.memberContact())
                .memberBirth(request.memberBirth())
                .memberEmail(request.memberEmail())
                .memberState(MemberState.ACTIVE)
                .memberRole(MemberRole.MEMBER)
                .memberLastestLoginAt(LocalDate.now())
                .memberPoint(0)
                .memberAccumulateAmount(0)
                .grade(defaultGrade)
                .build();

        memberRepository.save(member);
    }

    // 로그인
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 인증 수행. Spring Security가 DTO의 memberEmail을 'Username'으로 사용하여 인증을 시도
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.memberEmail(), request.memberPassword());

        // CustomUserDetailsService가 호출되어 DB 검증
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 인증 성공 후 CustomUserDetails에서 memberId(PK) 추출 <- JWT 생성 시 memberEmail이 아니라 memberId를 사용하기 위해서
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long memberId = userDetails.getMemberId();

        String role = getRole(authentication);

        // 토큰 발급 및 Redis 저장
        return generateTokens(memberId, role);
    }

    // 재발급 로직
    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token is null");
        }

        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("Refresh token expired");
        }

        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("refresh")) {
            throw new IllegalArgumentException("Invalid token category");
        }

        // Redis에서 토큰 조회 (없으면 예외 발생)
        RefreshToken storedToken = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        Long memberId = storedToken.getMemberId();
        String role = storedToken.getRole(); // Redis에 저장된 Role 사용

        // 기존 토큰 삭제 (Refresh Token Rotation)
        refreshTokenRepository.deleteById(refreshToken);

        // 새 토큰 발급 및 Redis 저장
        return generateTokens(memberId, role);
    }

    // 로그아웃
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && refreshTokenRepository.existsById(refreshToken)) {
            refreshTokenRepository.deleteById(refreshToken);
        }
    }

    // 토큰 생성 및 Redis 저장 공통화
    private TokenResponse generateTokens(Long memberId, String role) {
        long accessExpire = 1800000L;      // 30분
        long refreshExpire = 86400000L;   // 24시간

        String accessToken = jwtUtil.createJwt(memberId, "access", role, accessExpire);
        String refreshToken = jwtUtil.createJwt(memberId, "refresh", role, refreshExpire);

        // Redis에 저장 (memberId, role 포함)
        refreshTokenRepository.save(new RefreshToken(refreshToken, memberId, role));

        return new TokenResponse(accessToken, refreshToken);
    }

    private String getRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        return iterator.next().getAuthority();
    }
}