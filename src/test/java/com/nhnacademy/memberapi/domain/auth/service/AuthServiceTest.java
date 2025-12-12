package com.nhnacademy.memberapi.domain.auth.service;

import com.nhnacademy.memberapi.domain.auth.dto.CustomUserDetails;
import com.nhnacademy.memberapi.domain.auth.dto.LoginRequest;
import com.nhnacademy.memberapi.domain.auth.dto.TokenResponse;
import com.nhnacademy.memberapi.domain.auth.entity.RefreshToken;
import com.nhnacademy.memberapi.domain.auth.jwt.JWTUtil;
import com.nhnacademy.memberapi.domain.auth.repository.RefreshTokenRepository;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import com.nhnacademy.memberapi.global.error.exception.InvalidRefreshTokenException;
import com.nhnacademy.memberapi.global.error.exception.MemberStateConflictException;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private Member mockMember;
    private CustomUserDetails mockUserDetails;
    private LoginRequest mockLoginRequest;

    @BeforeEach
    void setUp() {
        mockMember = Member.builder()
                .memberId(1L)
                .memberEmail("test@nhn.com")
                .memberPassword("encoded_password")
                .memberState(MemberState.ACTIVE)
                .memberRole(MemberRole.MEMBER)
                .memberLastestLoginAt(java.time.LocalDate.of(2025, 1, 1))
                .build();

        mockUserDetails = new CustomUserDetails(mockMember);
        mockLoginRequest = new LoginRequest("test@nhn.com", "password");
    }

    @Test
    @DisplayName("로그인 성공_토큰 발급 및 로그인 시간 갱신")
    void login_Success() {
        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(
                mockUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER")));

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(mockAuthentication);
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(mockMember));
        given(jwtUtil.createJwt(anyLong(), eq("access"), anyString(), anyLong())).willReturn("mockAccessToken");
        given(jwtUtil.createJwt(anyLong(), eq("refresh"), anyString(), anyLong())).willReturn("mockRefreshToken");

        TokenResponse response = authService.login(mockLoginRequest);

        assertThat(response.accessToken()).isEqualTo("mockAccessToken");

        verify(memberRepository).save(mockMember);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 실패_휴면 계정")
    void login_Fail_Dormant() {
        mockMember.setMemberState(MemberState.DORMANT);
        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(
                mockUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER")));

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(mockAuthentication);
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(mockMember));

        assertThatThrownBy(() -> authService.login(mockLoginRequest))
                .isInstanceOf(MemberStateConflictException.class)
                .hasFieldOrPropertyWithValue("state", MemberState.DORMANT);
    }

    @Test
    @DisplayName("토큰 재발급 성공_Refresh Token Rotation")
    void reissue_Success() {
        String oldRefreshToken = "oldRefreshToken";
        Long memberId = 1L;
        String role = "MEMBER";
        RefreshToken storedToken = new RefreshToken(oldRefreshToken, memberId, role);
        TokenResponse newTokens = new TokenResponse("newAccessToken", "newRefreshToken");

        given(jwtUtil.isExpired(oldRefreshToken)).willReturn(false);
        given(jwtUtil.getCategory(oldRefreshToken)).willReturn("refresh");
        given(refreshTokenRepository.findById(oldRefreshToken)).willReturn(Optional.of(storedToken));

        given(jwtUtil.createJwt(anyLong(), eq("access"), anyString(), anyLong())).willReturn(newTokens.accessToken());
        given(jwtUtil.createJwt(anyLong(), eq("refresh"), anyString(), anyLong())).willReturn(newTokens.refreshToken());

        TokenResponse response = authService.reissue(oldRefreshToken);

        assertThat(response.accessToken()).isEqualTo("newAccessToken");
        verify(refreshTokenRepository).deleteById(oldRefreshToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("재발급 실패_만료된 Refresh Token")
    void reissue_Fail_Expired() {
        String expiredToken = "expiredToken";
        given(jwtUtil.isExpired(expiredToken)).willThrow(ExpiredJwtException.class);

        assertThatThrownBy(() -> authService.reissue(expiredToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("expired");
        verify(refreshTokenRepository, never()).findById(anyString());
    }

    @Test
    @DisplayName("로그아웃 성공_Access Token 블랙리스트 등록")
    void logout_Success() {
        String accessTokenWithBearer = "Bearer ValidAccessToken";
        String refreshToken = "ValidRefreshToken";
        String token = "ValidAccessToken";

        long expirationTime = new Date().getTime() + 10000;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        given(jwtUtil.getExpiration(token)).willReturn(expirationTime);
        given(refreshTokenRepository.existsById(refreshToken)).willReturn(true);

        authService.logout(refreshToken, accessTokenWithBearer);

        verify(refreshTokenRepository).deleteById(refreshToken);
        verify(valueOperations).set(eq("BL:" + token), eq("logout"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("로그아웃 성공_만료된 Access Token은 블랙리스트 미등록")@MockitoSettings(strictness = Strictness.LENIENT)
    void logout_Success_ExpiredAccessToken() {
        String expiredAccessToken = "Bearer ExpiredToken";
        String token = "ExpiredToken";

        long expirationTime = new Date().getTime() - 1000;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(jwtUtil.getExpiration(token)).willReturn(expirationTime);

        authService.logout("nonExistToken", expiredAccessToken);

        verify(refreshTokenRepository, never()).deleteById(any());
        verify(valueOperations, never()).set(any(), any(), anyLong(), any());
    }
}