package com.nhnacademy.memberapi.service;

import com.nhnacademy.memberapi.dto.request.*;
import com.nhnacademy.memberapi.dto.response.MemberResponse;
import com.nhnacademy.memberapi.entity.*;
import com.nhnacademy.memberapi.exception.InvalidVerificationCodeException;
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
    private final EmailService emailService;

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
                .memberEmail(request.memberEmail())
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

        // DTO를 Entity로 변환하고 양방향 관계 설정
        Address newAddress = Address.fromDto(request.address());
        // 양방향 관계 설정, Addresses를 리스트에 추가해주는 헬퍼 메서드
        addAddressToMember(member, newAddress);

        memberRepository.save(member);
    }

    // 회원탈퇴 (Soft Delete)
    public void withdrawMember(Long memberId, String refreshToken) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found"));

        // 탈퇴 상태로 변경
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

    // 소셜 회원가임
    public void socialSignupMember(SocialSignupRequest request) {
        if (memberRepository.existsByMemberEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        Grade defaultGrade = gradeRepository.findByGradeName(GradeName.COMMON)
                .orElseGet(() -> gradeRepository.save(Grade.builder().gradeName(GradeName.COMMON).gradeCondition(0).gradePointRatio(1).build()));

        Member member = Member.builder()
                .memberEmail(request.email())
                .memberPassword(UUID.randomUUID().toString()) // 비밀번호 랜덤
                .memberName(request.name())
                .memberBirth(request.birthDate()) // 생일 입력 받아서 저장
                .memberContact(request.contact()) // 없으면 null
                .memberState(MemberState.ACTIVE)
                .memberRole(MemberRole.MEMBER)
                .memberLastestLoginAt(LocalDate.now())
                .memberOauthId(request.memberOauthId())
                .memberPoint(0)
                .memberAccumulateAmount(0)
                .grade(defaultGrade)
                .build();

        // 주소 입력받아서 넣어야 함...
        Address newAddress = Address.fromDto(request.address());
        addAddressToMember(member, newAddress);

        memberRepository.save(member);
    }

    // 비밀번호 재설정
    public void resetPassword(PasswordResetRequest request) {
        // 입력한 이메일을 가진 회원이 있고
        Member member = memberRepository.findByMemberEmail(request.memberEmail())
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 회원을 찾을 수 없습니다."));

        // 그 이메일로 인증 성공 시
        boolean isVerified = emailService.verifyCode(request.memberEmail(), request.verificationCode());

        if (!isVerified) {
            throw new InvalidVerificationCodeException("인증 코드가 일치하지 않거나 만료되었습니다.");
        }

        // 비밀번호 암호화 및 재설정
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        member.setMemberPassword(encodedPassword);
    }

    // 아이디 찾기
    public String findMemberEmail(FindMemberIdRequest request) {
        Member member = memberRepository.findByMemberNameAndMemberContact(request.memberName(), request.memberContact())
                .orElseThrow(() -> new UsernameNotFoundException("Member not found"));

        String email = member.getMemberEmail();
        // 이메일 마스킹 처리
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return email.replaceAll("(?<=.{1}).(?=.*@)", "*");
        }
        String maskedEmail = email.substring(0, 2) + "****" + email.substring(atIndex - 4);

        return maskedEmail + email.substring(atIndex);
    }

    private void addAddressToMember(Member member, Address address) {
        // Address -> Member 관계 설정 (ManyToOne)
        address.setMember(member);
        // Member -> Address 관계 설정 (OneToMany)
        // Member 엔티티의 addresses 리스트에 추가
        member.getAddresses().add(address);
    }
}