package com.nhnacademy.memberapi.domain.auth.service;

import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import com.nhnacademy.memberapi.global.error.exception.MemberStateConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("정상 회원(ACTIVE) 로그인 성공")
    void loadUserByUsername_success() {
        String email = "active@test.com";
        Member member = Member.builder()
                .memberId(1L)
                .memberEmail(email)
                .memberRole(MemberRole.MEMBER)
                .memberState(MemberState.ACTIVE) // 정상 상태
                .build();

        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.of(member));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
    }

    @Test
    @DisplayName("존재하지 않는 회원은 UsernameNotFoundException 발생")
    void loadUserByUsername_notFound() {
        String email = "ghost@test.com";
        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("휴면 회원(DORMANT)은 MemberStateConflictException 발생")
    void loadUserByUsername_dormant() {
        String email = "dormant@test.com";
        Member member = Member.builder()
                .memberEmail(email)
                .memberState(MemberState.DORMANT) // 휴면 상태
                .build();

        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(MemberStateConflictException.class)
                .hasMessageContaining("휴면 계정입니다");
    }

    @Test
    @DisplayName("탈퇴 회원(WITHDRAWAL)은 MemberStateConflictException 발생")
    void loadUserByUsername_withdrawal() {
        String email = "withdrawal@test.com";
        Member member = Member.builder()
                .memberEmail(email)
                .memberState(MemberState.WITHDRAWAL)
                .build();

        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(MemberStateConflictException.class)
                .hasMessageContaining("탈퇴한 회원입니다");
    }
}