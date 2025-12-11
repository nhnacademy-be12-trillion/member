package com.nhnacademy.memberapi.domain.auth.jwt;

import com.nhnacademy.memberapi.domain.auth.dto.oauth2.CustomOAuth2User;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.domain.auth.entity.RefreshToken;
import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import com.nhnacademy.memberapi.domain.auth.repository.RefreshTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SocialLoginHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String email = customUserDetails.getEmail();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.iterator().next().getAuthority();

        if ("ROLE_GUEST".equals(role)) {
            // 신규 회원은 쿠키에 Register Token
            String registerToken = jwtUtil.createJwt(0L, "register", "ROLE_GUEST", 600000L);

            // 쿠키 생성
            response.addCookie(createCookie("register_token", registerToken, 600)); // 10분
            // 정보 전달용 (화면 표시용) - 이메일/이름은 보안 민감도가 낮으므로 파라미터로 넘겨도 됨
            String encodedName = URLEncoder.encode(customUserDetails.getName(), StandardCharsets.UTF_8);

            // OAuth ID 추출
            String oauthId = customUserDetails.getProviderId();

            // memberOauthId 파라미터 추가
            response.sendRedirect("/signup.html?email=" + email + "&name=" + encodedName + "&oauthId=" + oauthId);

        } else {
            // 기존 회원은 쿠키에 Access/Refresh Token
            Optional<Member> memberOp = memberRepository.findByMemberEmail(email);
            if(memberOp.isEmpty()) {
                response.sendRedirect("/login?error=not_found");
                return;
            }
            Member member = memberOp.get();

            // 휴면 계정 체크
            if (member.getMemberState() == MemberState.DORMANT) {
                // 토큰 생성 및 저장 로직을 실행하지 않음

                // 휴면 해제 페이지로 리다이렉트
                // todo
                String encodedEmail = URLEncoder.encode(member.getMemberEmail(), StandardCharsets.UTF_8);
                response.sendRedirect("/dormant-auth.html?email=" + encodedEmail);
                return;
            }

            String accessToken = jwtUtil.createJwt(member.getMemberId(), "access", "ROLE_MEMBER", 1800000L);
            String refreshToken = jwtUtil.createJwt(member.getMemberId(), "refresh", "ROLE_MEMBER", 86400000L);

            refreshTokenRepository.save(new RefreshToken(refreshToken, member.getMemberId(), "ROLE_MEMBER"));

            // 헤더 설정 (Set-Cookie)
            response.addCookie(createCookie("access_token", accessToken, 1800)); // 30분
            response.addCookie(createCookie("refresh_token", refreshToken, 86400)); // 24시간

            // URL 파라미터 없이 깔끔하게 리다이렉트
            response.sendRedirect("/login-success.html");
        }
    }

    // 쿠키 생성 유틸 메서드
    private Cookie createCookie(String key, String value, int maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setHttpOnly(false); // JS에서 읽을 수 있게 false (보안 강화하려면 true로 하고 API 통신만 해야 함)
        // cookie.setSecure(true); // todo HTTPS 적용 시 주석 해제
        return cookie;
    }
}