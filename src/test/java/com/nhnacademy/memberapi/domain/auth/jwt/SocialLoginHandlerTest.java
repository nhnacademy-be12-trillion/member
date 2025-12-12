package com.nhnacademy.memberapi.domain.auth.jwt;

import com.nhnacademy.memberapi.domain.auth.dto.oauth2.CustomOAuth2User;
import com.nhnacademy.memberapi.domain.auth.entity.RefreshToken;
import com.nhnacademy.memberapi.domain.auth.repository.RefreshTokenRepository;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SocialLoginHandlerTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private SocialLoginHandler socialLoginHandler;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomOAuth2User customOAuth2User;

    @Test
    @DisplayName("신규 회원(GUEST)일 경우 회원가입 페이지로 리다이렉트된다")
    void onAuthenticationSuccess_guest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(authentication.getPrincipal()).willReturn(customOAuth2User);
        given(customOAuth2User.getEmail()).willReturn("new@test.com");
        given(customOAuth2User.getName()).willReturn("홍길동");
        given(customOAuth2User.getProviderId()).willReturn("provider123");

        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_GUEST")))
                .when(authentication).getAuthorities();

        String fakeRegisterToken = "fakeRegisterToken";
        given(jwtUtil.createJwt(anyLong(), eq("register"), eq("ROLE_GUEST"), anyLong()))
                .willReturn(fakeRegisterToken);

        socialLoginHandler.onAuthenticationSuccess(request, response, authentication);

        String expectedName = URLEncoder.encode("홍길동", StandardCharsets.UTF_8);
        String redirectUrl = response.getRedirectedUrl();
        assertThat(redirectUrl).contains("/signup.html");
        assertThat(redirectUrl).contains("email=new@test.com");
        assertThat(redirectUrl).contains("name=" + expectedName);
        assertThat(redirectUrl).contains("oauthId=provider123");

        Cookie cookie = response.getCookie("register_token");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo(fakeRegisterToken);
    }

    @Test
    @DisplayName("기존 회원(ACTIVE)일 경우 로그인 성공 처리 및 토큰 발급")
    void onAuthenticationSuccess_member_active() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String email = "member@test.com";
        Long memberId = 100L;

        given(authentication.getPrincipal()).willReturn(customOAuth2User);
        given(customOAuth2User.getEmail()).willReturn(email);

        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER")))
                .when(authentication).getAuthorities();

        Member member = Member.builder()
                .memberId(memberId)
                .memberEmail(email)
                .memberState(MemberState.ACTIVE)
                .build();
        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.of(member));

        String accessToken = "access123";
        String refreshToken = "refresh123";
        given(jwtUtil.createJwt(memberId, "access", "ROLE_MEMBER", 1800000L)).willReturn(accessToken);
        given(jwtUtil.createJwt(memberId, "refresh", "ROLE_MEMBER", 86400000L)).willReturn(refreshToken);

        socialLoginHandler.onAuthenticationSuccess(request, response, authentication);

        verify(refreshTokenRepository).save(any(RefreshToken.class));
        assertThat(response.getCookie("access_token").getValue()).isEqualTo(accessToken);
        assertThat(response.getCookie("refresh_token").getValue()).isEqualTo(refreshToken);
        assertThat(response.getRedirectedUrl()).isEqualTo("/login-success.html");
    }

    @Test
    @DisplayName("기존 회원이지만 휴면(DORMANT) 상태일 경우 휴면 해제 페이지로 이동")
    void onAuthenticationSuccess_member_dormant() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String email = "dormant@test.com";

        given(authentication.getPrincipal()).willReturn(customOAuth2User);
        given(customOAuth2User.getEmail()).willReturn(email);

        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER")))
                .when(authentication).getAuthorities();

        Member dormantMember = Member.builder()
                .memberId(200L)
                .memberEmail(email)
                .memberState(MemberState.DORMANT)
                .build();
        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.of(dormantMember));

        socialLoginHandler.onAuthenticationSuccess(request, response, authentication);

        verify(refreshTokenRepository, never()).save(any());
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        assertThat(response.getRedirectedUrl()).isEqualTo("/dormant-auth.html?email=" + encodedEmail);
    }

    @Test
    @DisplayName("ROLE은 MEMBER인데 DB에 회원이 없는 경우 (예외상황)")
    void onAuthenticationSuccess_member_notFound() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String email = "ghost@test.com";

        given(authentication.getPrincipal()).willReturn(customOAuth2User);
        given(customOAuth2User.getEmail()).willReturn(email);

        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER")))
                .when(authentication).getAuthorities();

        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.empty());

        socialLoginHandler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("/login?error=not_found");
    }
}