package com.nhnacademy.memberapi.domain.auth.service;

import com.nhnacademy.memberapi.domain.auth.dto.oauth2.CustomOAuth2User;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import com.nhnacademy.memberapi.global.error.exception.OAuthEmailNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    @Spy
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    @DisplayName("구글 로그인: 신규 회원이면 ROLE_GUEST 반환")
    void loadUser_google_newUser() {
        OAuth2UserRequest request = mockRequest("google");

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "new@google.com");
        attributes.put("name", "New User");

        given(mockOAuth2User.getAttributes()).willReturn(attributes);
        doReturn(mockOAuth2User).when(customOAuth2UserService).processOAuth2UserDelegate(any());

        given(memberRepository.findByMemberEmail("new@google.com")).willReturn(Optional.empty());

        OAuth2User result = customOAuth2UserService.loadUser(request);

        assertThat(result).isInstanceOf(CustomOAuth2User.class);
        CustomOAuth2User customUser = (CustomOAuth2User) result;
        assertThat(customUser.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_GUEST");
    }

    @Test
    @DisplayName("페이코 로그인: 기존 회원이면 ROLE_MEMBER 반환")
    void loadUser_payco_existingUser() {
        OAuth2UserRequest request = mockRequest("payco");
        OAuth2User mockOAuth2User = mock(OAuth2User.class);

        Map<String, Object> memberMap = new HashMap<>();
        memberMap.put("idNo", "paycoId");
        memberMap.put("email", "exist@payco.com");
        memberMap.put("name", "페이코유저");

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("member", memberMap);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("data", dataMap);

        given(mockOAuth2User.getAttributes()).willReturn(attributes);
        doReturn(mockOAuth2User).when(customOAuth2UserService).processOAuth2UserDelegate(any());

        Member existingMember = Member.builder()
                .memberEmail("exist@payco.com")
                .memberRole(MemberRole.MEMBER)
                .build();
        given(memberRepository.findByMemberEmail("exist@payco.com")).willReturn(Optional.of(existingMember));

        OAuth2User result = customOAuth2UserService.loadUser(request);

        CustomOAuth2User customUser = (CustomOAuth2User) result;
        assertThat(customUser.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_MEMBER");
    }

    @Test
    @DisplayName("이메일 정보가 없으면 OAuthEmailNotFoundException 발생")
    void loadUser_noEmail() {
        OAuth2UserRequest request = mockRequest("google");
        OAuth2User mockOAuth2User = mock(OAuth2User.class);

        // 이메일 없이 이름만 있는 속성
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google_123");
        attributes.put("name", "No Email User");

        given(mockOAuth2User.getAttributes()).willReturn(attributes);
        doReturn(mockOAuth2User).when(customOAuth2UserService).processOAuth2UserDelegate(any());

        assertThatThrownBy(() -> customOAuth2UserService.loadUser(request))
                .isInstanceOf(OAuthEmailNotFoundException.class)
                .hasMessageContaining("이메일을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("지원하지 않는 Provider는 null 반환")
    void loadUser_unsupportedProvider() {
        OAuth2UserRequest request = mockRequest("naver");
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        doReturn(mockOAuth2User).when(customOAuth2UserService).processOAuth2UserDelegate(any());

        OAuth2User result = customOAuth2UserService.loadUser(request);

        assertThat(result).isNull();
    }

    private OAuth2UserRequest mockRequest(String registrationId) {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(registrationId)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("test-client-id")
                .redirectUri("http://localhost/login/oauth2/code/" + registrationId)
                .authorizationUri("https://provider.com/oauth2/authorize")
                .tokenUri("https://provider.com/oauth2/token")
                .userInfoUri("https://provider.com/userinfo")
                .userNameAttributeName("id")
                .clientName("Test Client")
                .build();

        return new OAuth2UserRequest(clientRegistration, mock(org.springframework.security.oauth2.core.OAuth2AccessToken.class));
    }
}