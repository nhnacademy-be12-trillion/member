package com.nhnacademy.memberapi.security;

import com.nhnacademy.memberapi.domain.auth.dto.CustomUserDetails;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockAuthUserSecurityContextFactory implements WithSecurityContextFactory<WithMockAuthUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockAuthUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Member mockMember = Member.builder()
                .memberId(1L)
                .memberEmail(annotation.email())
                .memberPassword("password")
                .memberName("test")
                .memberContact("010-1234-5678")
                .memberBirth(java.time.LocalDate.of(2000, 1, 1))
                .memberState(MemberState.ACTIVE)
                .memberLatestLoginAt(java.time.LocalDate.now())
                .memberRole(MemberRole.valueOf(annotation.role()))
                .memberPoint(0)
                .memberAccumulateAmount(0)
                .grade(null)
                .build();

        CustomUserDetails principal = new CustomUserDetails(mockMember);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        context.setAuthentication(authentication);
        return context;
    }
}