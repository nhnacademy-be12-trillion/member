package com.nhnacademy.memberapi.service;

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

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final GradeRepository gradeRepository;


    // 회원가입
    public void signupMember(MemberSignupRequest request) {

        // 1. 이메일 중복 검사
        if(memberRepository.existsByMemberEmail(request.memberEmail())){
            throw new UserAlreadyExistsException(request.memberEmail());
        }

        // 2. [핵심 수정] 등급 조회 시 데이터가 없으면 '새로 생성'하여 반환 (500 에러 방지)
        Grade defaultGrade = gradeRepository.findByGradeName(GradeName.COMMON)
                .orElseGet(() -> {
                    Grade newGrade = Grade.builder()
                            .gradeName(GradeName.COMMON)
                            .gradeCondition(0)     // 기본 조건 0
                            .gradePointRatio(1)    // 기본 적립률 1% (예시)
                            .build();
                    return gradeRepository.save(newGrade);
                });


        // 3. 회원 정보 생성
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

        // 4. 회원 저장
        memberRepository.save(member);
    }

    // 로그인 + JWT 발급은 LoginFilter, CustomUserDetailsService가 처리
}