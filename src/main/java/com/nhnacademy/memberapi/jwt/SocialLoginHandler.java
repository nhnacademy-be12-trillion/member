package com.nhnacademy.memberapi.jwt;

import com.nhnacademy.memberapi.dto.oauth2.CustomOAuth2User;
import com.nhnacademy.memberapi.entity.Member;
import com.nhnacademy.memberapi.entity.RefreshToken;
import com.nhnacademy.memberapi.repository.MemberRepository;
import com.nhnacademy.memberapi.repository.RefreshTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

        // 권한 확인 (ROLE_MEMBER vs ROLE_GUEST)
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.iterator().next().getAuthority();

        if ("ROLE_GUEST".equals(role)) {
            // 신규 회원 -> 생년월일 입력 페이지로 리다이렉트
            // 임시 토큰 생성 (유효기간 10분, category="register")
            // DB 저장이 안 된 상태이므로 memberId 대신 0L 혹은 임시값 사용, 중요한 건 email claim
            String registerToken = jwtUtil.createJwt(0L, "register", "ROLE_GUEST", 600000L); // createJwt 메서드 수정 필요 혹은 email 전용 메서드 추가

            // 프론트엔드의 추가 정보 입력 페이지 URL (쿼리 파라미터로 토큰 전달)
            // 실제로는 email 정보도 토큰 안에 claims로 넣는 게 안전함. 여기서는 기존 createJwt가 memberId 기반이라 가정하고 아래에서 보완 설명.
            response.sendRedirect("http://localhost:3000/signup/social?token=" + registerToken + "&email=" + email + "&name=" + customUserDetails.getName());

        } else {
            // 기존 회원 -> 로그인 처리
            Optional<Member> memberOp = memberRepository.findByMemberEmail(email);
            if(memberOp.isEmpty()) {
                response.sendRedirect("http://localhost:3000/login?error=not_found");
                return;
            }
            Member member = memberOp.get();

            // 토큰 발급
            String accessToken = jwtUtil.createJwt(member.getMemberId(), "access", "ROLE_MEMBER", 1800000L);
            String refreshToken = jwtUtil.createJwt(member.getMemberId(), "refresh", "ROLE_MEMBER", 86400000L);

            refreshTokenRepository.save(new RefreshToken(refreshToken, member.getMemberId(), "ROLE_MEMBER"));

            response.sendRedirect("http://localhost:3000/login-success?access=" + accessToken + "&refresh=" + refreshToken);
        }
    }
}