package com.nhnacademy.memberapi.service;

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
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final GradeRepository gradeRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;


    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, GradeRepository gradeRepository, AuthenticationManager authenticationManager, RefreshTokenRepository refreshTokenRepository, JWTUtil jwtUtil) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.gradeRepository = gradeRepository;
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
    }

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

    // 1. 로그인 로직
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 인증 수행
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.memberEmail(), request.memberPassword());

        // 여기서 CustomUserDetailsService가 호출되어 DB 검증이 일어납니다.
        Authentication authentication = authenticationManager.authenticate(authToken);

        String username = authentication.getName();
        String role = getRole(authentication);

        // 토큰 발급 및 Redis 저장
        return generateTokens(username, role);
    }

    // 2. 재발급 로직
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

        String username = storedToken.getUsername();
        String role = storedToken.getRole(); // 🚀 Redis에 저장된 Role 사용

        // 기존 토큰 삭제 (Refresh Token Rotation)
        refreshTokenRepository.deleteById(refreshToken);

        // 새 토큰 발급 및 Redis 저장
        return generateTokens(username, role);
    }

    // 3. 로그아웃 로직
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && refreshTokenRepository.existsById(refreshToken)) {
            refreshTokenRepository.deleteById(refreshToken);
        }
    }

    // 내부 메서드: 토큰 생성 및 Redis 저장 공통화
    private TokenResponse generateTokens(String username, String role) {
        long accessExpire = 5000L;      // 5초
        long refreshExpire = 86400000L;   // 24시간

        String accessToken = jwtUtil.createJwt("access", username, role, accessExpire);
        String refreshToken = jwtUtil.createJwt("refresh", username, role, refreshExpire);

        // 🚀 Redis에 저장 (username, role 포함)
        refreshTokenRepository.save(new RefreshToken(refreshToken, username, role));

        return new TokenResponse(accessToken, refreshToken);
    }

    private String getRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        return iterator.next().getAuthority();
    }
}