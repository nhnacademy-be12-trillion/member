package com.nhnacademy.memberapi.service;

import com.nhnacademy.memberapi.dto.request.MemberSignupRequest;
import com.nhnacademy.memberapi.dto.request.MemberUpdateRequest;
import com.nhnacademy.memberapi.dto.request.SocialSignupRequest;
import com.nhnacademy.memberapi.dto.response.MemberResponse;
import com.nhnacademy.memberapi.entity.*;
import com.nhnacademy.memberapi.exception.UserAlreadyExistsException;
import com.nhnacademy.memberapi.repository.GradeRepository;
import com.nhnacademy.memberapi.repository.MemberRepository;
import com.nhnacademy.memberapi.repository.RefreshTokenRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final GradeRepository gradeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public void signupMember(MemberSignupRequest request) {
        if (memberRepository.existsByMemberEmail(request.memberEmail())) {
            throw new UserAlreadyExistsException(request.memberEmail());
        }

        Grade defaultGrade = gradeRepository.findByGradeName(GradeName.COMMON)
                .orElseGet(() -> {
                    Grade newGrade = Grade.builder()
                            .gradeName(GradeName.COMMON)
                            .gradeCondition(0)
                            .gradePointRatio(1)
                            .build();
                    return gradeRepository.save(newGrade);
                });

        Member member = Member.builder()
                .memberPassword(passwordEncoder.encode(request.memberPassword()))
                .memberName(request.memberName())
                .memberContact(request.memberContact())
                .memberBirth(request.memberBirth())
                .memberEmail(request.memberEmail())
                .memberState(MemberState.ACTIVE)
                .memberRole(MemberRole.MEMBER)
                .memberLastestLoginAt(LocalDate.now())
                .memberPoint(0)
                .memberAccumulateAmount(0)
                .grade(defaultGrade)
                .build();

        memberRepository.save(member);
    }

    // 회원탈퇴 (Soft Delete)
    public void withdrawMember(Long memberId, String refreshToken) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found"));

        // 상태 변경 (탈퇴)
        member.setMemberState(MemberState.WITHDRAWAL);

        // 로그아웃 처리 (Refresh Token 삭제)
        if (refreshToken != null && refreshTokenRepository.existsById(refreshToken)) {
            refreshTokenRepository.deleteById(refreshToken);
        }
    }

    public void updateMember(Long memberId, @Valid MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found"));
        member.setMemberPassword(request.memberPassword());
        member.setMemberContact(request.memberContact());
        member.setMemberName(request.memberName());
        member.setMemberBirth(request.memberBirth());
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found"));
        return MemberResponse.fromEntity(member);
    }

    public void socialSignupMember(SocialSignupRequest request) {
        if (memberRepository.existsByMemberEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        Grade defaultGrade = gradeRepository.findByGradeName(GradeName.COMMON)
                .orElseGet(() -> gradeRepository.save(Grade.builder().gradeName(GradeName.COMMON).gradeCondition(0).gradePointRatio(1).build()));

        Member member = Member.builder()
                .memberEmail(request.email())
                .memberName(request.name())
                .memberPassword(UUID.randomUUID().toString()) // 비밀번호 랜덤
                .memberBirth(request.birthDate()) // 생일 입력 받아서 저장
                .memberContact(request.contact()) // 없으면 null
                .memberState(MemberState.ACTIVE)
                .memberRole(MemberRole.MEMBER)
                .memberLastestLoginAt(LocalDate.now())
                .memberPoint(0)
                .memberAccumulateAmount(0)
                .grade(defaultGrade)
                .build();

        memberRepository.save(member);
    }
}