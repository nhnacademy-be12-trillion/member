package com.nhnacademy.memberapi.service;

import com.nhnacademy.memberapi.dto.request.LoginRequest;
import com.nhnacademy.memberapi.dto.request.MemberSignupRequest;
import com.nhnacademy.memberapi.entity.*;
import com.nhnacademy.memberapi.exception.UserAlreadyExistsException;
import com.nhnacademy.memberapi.repository.GradeRepository;
import com.nhnacademy.memberapi.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private MemberRepository memberRepository;
    private PasswordEncoder passwordEncoder;
    private final GradeRepository gradeRepository;


    // 회원가입
    public void signupMember(MemberSignupRequest request) {

        if(memberRepository.existsByMemberEmail(request.memberEmail())){
            throw new UserAlreadyExistsException(request.memberEmail());
        }

        Grade defaultGrade = gradeRepository.findByGradeName(GradeName.COMMON)
                .orElseThrow(() -> new RuntimeException("COMMON 등급을 찾을 수 없습니다."));


        Member member = Member.builder()
                .memberPassword(passwordEncoder.encode(request.memberPassword()))
                .memberName(request.memberName())
                .memberContact(request.memberContact())
                .memberBirth(request.memberBirth())
                .memberState(MemberState.ACTIVE)
                .memberRole(MemberRole.MEMBER)
                .memberLastestLoginAt(LocalDate.now())
                .memberPoint(0)
                .memberAccumulateAmount(0)
                .grade(defaultGrade)
                .build();

        memberRepository.save(member);
    }

    // 로그인 + JWT 발급

}
