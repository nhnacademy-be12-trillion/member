package com.nhnacademy.memberapi.domain.member.service;

import com.nhnacademy.memberapi.domain.address.entity.Address;
import com.nhnacademy.memberapi.domain.grade.entity.Grade;
import com.nhnacademy.memberapi.domain.grade.entity.GradeName;
import com.nhnacademy.memberapi.domain.grade.repository.GradeRepository;
import com.nhnacademy.memberapi.domain.member.dto.*;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import com.nhnacademy.memberapi.global.error.exception.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final GradeRepository gradeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final DoorayService doorayService;

    // 회원가입
    public void signupMember(MemberSignupRequest request) {
        if (memberRepository.existsByMemberEmail(request.memberEmail())) {
            throw new UserAlreadyExistsException(request.memberEmail());
        }
        boolean isVerified = emailService.verifyCode(request.memberEmail(), request.verificationCode());
        if (!isVerified) {
            throw new InvalidVerificationCodeException("인증 코드가 일치하지 않거나 만료되었습니다.");
        }

        Grade defaultGrade = gradeRepository.findByGradeName(GradeName.COMMON)
                .orElseGet(() -> {
                    Grade newGrade = Grade.builder()
                            .gradeName(GradeName.COMMON)
                            .gradeCondition(0)
                            .gradePointRatio(BigDecimal.valueOf(0.01))
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
                .memberLatestLoginAt(LocalDate.now())
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

    // 회원 탈퇴 (상태만 변경)
    public void withdrawMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("Member not found"));
        // 탈퇴 상태로 변경
        member.setMemberState(MemberState.WITHDRAWAL);
    }

    public void updateMember(Long memberId, @Valid MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("Member not found"));
        if (request.memberContact() != null && !request.memberContact().isBlank()) {
            member.setMemberContact(request.memberContact());
        }
        if (request.memberName() != null && !request.memberName().isBlank()) {
            member.setMemberName(request.memberName());
        }
        if (request.memberBirth() != null) {
            member.setMemberBirth(request.memberBirth());
        }
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("Member not found"));

        // 조회할 때 탈퇴 및 휴면 상태를 검사해서 토큰이 살아있는 경우 방지
        if (member.getMemberState() == MemberState.WITHDRAWAL) {
            throw new AccessDeniedException("이미 탈퇴한 회원입니다. 접근이 불가능합니다.");
        }
        if (member.getMemberState() == MemberState.DORMANT) {
            throw new AccessDeniedException("휴면 계정입니다. 인증이 필요합니다.");
        }

        return MemberResponse.fromEntity(member);
    }

    // 소셜 회원가임
    public void socialSignupMember(SocialSignupRequest request) {
        if (memberRepository.existsByMemberEmail(request.memberEmail())) {
            throw new UserAlreadyExistsException(request.memberEmail());
        }

        Grade defaultGrade = gradeRepository.findByGradeName(GradeName.COMMON)
                .orElseGet(() -> gradeRepository.save(Grade.builder().gradeName(GradeName.COMMON).gradeCondition(0).gradePointRatio(BigDecimal.valueOf(0.01)).build()));

        Member member = Member.builder()
                .memberEmail(request.memberEmail())
                .memberPassword(UUID.randomUUID().toString()) // 비밀번호 랜덤
                .memberName(request.memberName())
                .memberBirth(request.memberBirth()) // 생일 입력 받아서 저장
                .memberContact(request.memberContact()) // 없으면 null
                .memberState(MemberState.ACTIVE)
                .memberRole(MemberRole.MEMBER)
                .memberLatestLoginAt(LocalDate.now())
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
    public void resetPassword(@Valid PasswordResetRequest request) {
        // 입력한 이메일을 가진 회원이 있고
        Member member = memberRepository.findByMemberEmail(request.memberEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 이메일을 가진 회원을 찾을 수 없습니다."));
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
                .orElseThrow(() -> new UserNotFoundException("Member not found"));

        String email = member.getMemberEmail();
        // 이메일 마스킹 처리
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return email.replaceAll("(?<=.{1}).(?=.*@)", "*");
        }
        return email.substring(0, 2) + "****" + email.substring(atIndex - 4);
    }

    private void addAddressToMember(Member member, Address address) {
        // Address -> Member 관계 설정 (ManyToOne)
        address.setMember(member);
        // Member -> Address 관계 설정 (OneToMany)
        // Member 엔티티의 addresses 리스트에 추가
        member.getAddresses().add(address);
    }

    // 회원가입 시 중복 이메일 확인 및 이메일 인증
    public void sendSignupVerificationCode(String email) {
        if (memberRepository.existsByMemberEmail(email)) {
            throw new UserAlreadyExistsException("이미 가입된 이메일입니다.");
        }
        emailService.sendVerificationCode(email);
    }

    // 비밀번호 재설정 시 회원인지 확인 및 이메일 인증
    public void sendResetPasswordVerificationCode(String email) {
        if (!memberRepository.existsByMemberEmail(email)) {
            throw new UserNotFoundException("가입되지 않은 이메일입니다.");
        }
        emailService.sendVerificationCode(email);
    }

    // 휴면 해제 인증번호 요청
    public void requestDormantRelease(String memberEmail, String doorayHookUrl) {
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다."));

        if (member.getMemberState() != MemberState.DORMANT) {
            throw new IllegalStateException("휴면 계정이 아닙니다.");
        }

        // 인증번호 발송
        doorayService.sendDormantVerificationCode(memberEmail, doorayHookUrl);
    }

    // 휴면 해제 인증 수행 및 상태 변경
    public void processDormantRelease(String memberEmail, String verificationCode) {
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다."));

        // 인증번호 검증
        boolean isVerified = doorayService.verifyDormantCode(memberEmail, verificationCode);
        if (!isVerified) {
            throw new InvalidVerificationCodeException("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        // 상태 변경 (DORMANT -> ACTIVE)
        member.setMemberState(MemberState.ACTIVE);
        member.setMemberLatestLoginAt(LocalDate.now()); // 로그인 날짜 최신화

        memberRepository.save(member);
    }

    @Transactional
    public void updateSocialInfo(Long memberId, SocialSignupRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));
        member.setMemberBirth(request.memberBirth());

        if (StringUtils.hasText(request.memberContact())) {
            member.setMemberContact(request.memberContact());
        }

        member.setMemberRole(MemberRole.MEMBER);
    }
}