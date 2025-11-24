package com.nhnacademy.memberapi.service;

import com.nhnacademy.memberapi.dto.oauth2.*;
import com.nhnacademy.memberapi.entity.Member;
import com.nhnacademy.memberapi.repository.MemberRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    public CustomOAuth2UserService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        String registrationId = request.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response;
        if(registrationId.equals("google")){
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }else if (registrationId.equals("kakao")){
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        }else if (registrationId.equals("naver")){
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        // DB에서 이메일로 조회
        Optional<Member> existMember = memberRepository.findByMemberEmail(oAuth2Response.getEmail());

        if (existMember.isPresent()) {
            // 이미 가입된 회원 (ROLE_MEMBER)
            return new CustomOAuth2User(oAuth2Response, "ROLE_" + existMember.get().getMemberRole().name());
        } else {
            // 신규 회원. 아직 DB 저장 안 한 상태 (ROLE_GUEST)
            return new CustomOAuth2User(oAuth2Response, "ROLE_GUEST");
        }
    }


}
