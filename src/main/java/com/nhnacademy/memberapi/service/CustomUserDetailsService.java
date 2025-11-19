package com.nhnacademy.memberapi.service;

import com.nhnacademy.memberapi.dto.CustomUserDetails;
import com.nhnacademy.memberapi.entity.Member;
import com.nhnacademy.memberapi.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Member memberData = memberRepository.findByMemberEmail(username).orElse(null);
        if (Objects.nonNull(memberData)) {
            return new CustomUserDetails(memberData);
        }
        return null;
    }
}
