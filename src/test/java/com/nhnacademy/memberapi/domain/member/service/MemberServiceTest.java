package com.nhnacademy.memberapi.domain.member.service;

import com.nhnacademy.memberapi.domain.address.dto.AddressCreateRequest;
import com.nhnacademy.memberapi.domain.address.entity.Address;
import com.nhnacademy.memberapi.domain.address.repository.AddressRepository;
import com.nhnacademy.memberapi.domain.grade.entity.Grade;
import com.nhnacademy.memberapi.domain.grade.entity.GradeName;
import com.nhnacademy.memberapi.domain.grade.repository.GradeRepository;
import com.nhnacademy.memberapi.domain.member.dto.*;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.domain.member.event.MemberSignedUpEvent;
import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import com.nhnacademy.memberapi.domain.point.service.PointHistoryService;
import com.nhnacademy.memberapi.global.error.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private DoorayService doorayService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PointHistoryService pointHistoryService;

    private Member createMember(Long id, String email) {
        Grade grade = new Grade(1L, GradeName.COMMON, BigDecimal.valueOf(0.01), 0);
        return Member.builder()
                .memberId(id)
                .memberEmail(email)
                .memberPassword("encodedPassword")
                .memberName("테스터")
                .memberContact("010-1234-5678")
                .memberBirth(LocalDate.of(1990, 1, 1))
                .memberState(MemberState.ACTIVE)
                .memberRole(MemberRole.MEMBER)
                .memberPoint(0)
                .memberAccumulateAmount(0)
                .memberLatestLoginAt(LocalDate.now())
                .grade(grade)
                .build();
    }

    // --- 회원가입 (Signup) ---

    @Test
    @DisplayName("회원가입 성공 - 기본 등급 존재 시")
    void signupMember() {
        AddressCreateRequest addressRequest = new AddressCreateRequest("12345", "도로명", "상세", "집");
        MemberSignupRequest request = new MemberSignupRequest(
                "test@nhn.com", "123456", "password", "테스터",
                "010-1234-5678", LocalDate.of(1990, 1, 1), addressRequest
        );
        Grade defaultGrade = new Grade(1L, GradeName.COMMON, BigDecimal.valueOf(0.01), 0);

        given(memberRepository.existsByMemberEmail(request.memberEmail())).willReturn(false);
        given(memberRepository.existsByMemberContact(request.memberContact())).willReturn(false);
        given(emailService.verifyCode(request.memberEmail(), request.verificationCode())).willReturn(true);
        given(gradeRepository.findByGradeName(GradeName.COMMON)).willReturn(Optional.of(defaultGrade));
        given(passwordEncoder.encode(request.memberPassword())).willReturn("encodedPassword");

        memberService.signupMember(request);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();

        assertThat(savedMember.getMemberEmail()).isEqualTo(request.memberEmail());
        assertThat(savedMember.getAddresses()).hasSize(1);
        verify(pointHistoryService).awardSignupPoints(savedMember.getMemberId());
        verify(eventPublisher).publishEvent(any(MemberSignedUpEvent.class));
    }

    @Test
    @DisplayName("회원가입 성공 - 기본 등급 없을 시 새로 생성")
    void signupMember_CreateNewGrade() {
        AddressCreateRequest addressRequest = new AddressCreateRequest("12345", "도로명", "상세", "집");
        MemberSignupRequest request = new MemberSignupRequest(
                "test@nhn.com", "123456", "password", "테스터",
                "010-1234-5678", LocalDate.of(1990, 1, 1), addressRequest
        );

        given(memberRepository.existsByMemberEmail(request.memberEmail())).willReturn(false);
        given(memberRepository.existsByMemberContact(request.memberContact())).willReturn(false);
        given(emailService.verifyCode(request.memberEmail(), request.verificationCode())).willReturn(true);

        // 등급이 없어서 Optional.empty() 반환 -> save 호출 시뮬레이션
        given(gradeRepository.findByGradeName(GradeName.COMMON)).willReturn(Optional.empty());
        given(gradeRepository.save(any(Grade.class))).willAnswer(invocation -> invocation.getArgument(0));

        given(passwordEncoder.encode(request.memberPassword())).willReturn("encodedPassword");

        memberService.signupMember(request);

        verify(gradeRepository).save(any(Grade.class));
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signupMember_Fail_EmailExists() {
        MemberSignupRequest request = new MemberSignupRequest(
                "duplicate@nhn.com", "123456", "pwd", "name", "010-1234-5678", LocalDate.now(), null
        );
        given(memberRepository.existsByMemberEmail(request.memberEmail())).willReturn(true);

        assertThatThrownBy(() -> memberService.signupMember(request))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("회원가입 실패 - 연락처 중복")
    void signupMember_Fail_ContactExists() {
        MemberSignupRequest request = new MemberSignupRequest(
                "test@nhn.com", "123456", "pwd", "name", "010-9999-9999", LocalDate.now(), null
        );
        given(memberRepository.existsByMemberEmail(request.memberEmail())).willReturn(false);
        given(memberRepository.existsByMemberContact(request.memberContact())).willReturn(true);

        assertThatThrownBy(() -> memberService.signupMember(request))
                .isInstanceOf(DuplicateMemberContactException.class);
    }

    @Test
    @DisplayName("회원가입 실패 - 인증코드 불일치")
    void signupMember_Fail_InvalidCode() {
        MemberSignupRequest request = new MemberSignupRequest(
                "test@nhn.com", "wrongCode", "pwd", "name", "010-1234-5678", LocalDate.now(), null
        );
        given(memberRepository.existsByMemberEmail(request.memberEmail())).willReturn(false);
        given(memberRepository.existsByMemberContact(request.memberContact())).willReturn(false);
        given(emailService.verifyCode(request.memberEmail(), request.verificationCode())).willReturn(false);

        assertThatThrownBy(() -> memberService.signupMember(request))
                .isInstanceOf(InvalidVerificationCodeException.class);
    }

    // --- 회원 조회 (GetMember) ---

    @Test
    @DisplayName("회원 조회 성공")
    void getMember_Success() {
        Member member = createMember(1L, "test@nhn.com");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        MemberResponse response = memberService.getMember(1L);

        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.memberEmail()).isEqualTo("test@nhn.com");
    }

    @Test
    @DisplayName("회원 조회 실패 - 존재하지 않음")
    void getMember_Fail_NotFound() {
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMember(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("회원 조회 실패 - 탈퇴한 회원")
    void getMember_Fail_Withdrawal() {
        Member member = createMember(1L, "test@nhn.com");
        member.setMemberState(MemberState.WITHDRAWAL);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.getMember(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("이미 탈퇴한 회원");
    }

    @Test
    @DisplayName("회원 조회 실패 - 휴면 계정")
    void getMember_Fail_Dormant() {
        Member member = createMember(1L, "test@nhn.com");
        member.setMemberState(MemberState.DORMANT);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.getMember(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("휴면 계정");
    }

    // --- 회원 수정 (Update) ---

    @Test
    @DisplayName("회원 수정 성공 - 모든 필드 변경")
    void updateMember_AllFields() {
        Member member = createMember(1L, "test@nhn.com");
        MemberUpdateRequest request = new MemberUpdateRequest(
                "010-9999-9999", "새이름", LocalDate.of(2000, 1, 1)
        );

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        // 연락처가 바뀌었으므로 중복 체크
        given(memberRepository.existsByMemberContact(request.memberContact())).willReturn(false);

        memberService.updateMember(1L, request);

        assertThat(member.getMemberContact()).isEqualTo("010-9999-9999");
        assertThat(member.getMemberName()).isEqualTo("새이름");
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("회원 수정 성공 - 연락처 변경 안함 (중복체크 건너뜀)")
    void updateMember_SameContact() {
        Member member = createMember(1L, "test@nhn.com");
        MemberUpdateRequest request = new MemberUpdateRequest(
                "010-1234-5678", "새이름", null // 연락처 기존과 동일
        );

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        // existsByMemberContact는 호출되지 않아야 함

        memberService.updateMember(1L, request);

        assertThat(member.getMemberName()).isEqualTo("새이름");
        verify(memberRepository, never()).existsByMemberContact(anyString());
    }

    @Test
    @DisplayName("회원 수정 실패 - 중복된 연락처")
    void updateMember_Fail_DuplicateContact() {
        Member member = createMember(1L, "test@nhn.com");
        MemberUpdateRequest request = new MemberUpdateRequest(
                "010-9999-9999", "새이름", null
        );

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.existsByMemberContact(request.memberContact())).willReturn(true);

        assertThatThrownBy(() -> memberService.updateMember(1L, request))
                .isInstanceOf(DuplicateMemberContactException.class);
    }

    @Test
    @DisplayName("회원 수정 실패 - 회원 찾을 수 없음")
    void updateMember_Fail_NotFound() {
        given(memberRepository.findById(1L)).willReturn(Optional.empty());
        MemberUpdateRequest request = new MemberUpdateRequest("010-0000-0000", "name", null);

        assertThatThrownBy(() -> memberService.updateMember(1L, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- 회원 탈퇴 (Withdraw) ---

    @Test
    @DisplayName("회원 탈퇴 성공")
    void withdrawMember() {
        Member member = createMember(1L, "test@nhn.com");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        memberService.withdrawMember(1L);

        assertThat(member.getMemberState()).isEqualTo(MemberState.WITHDRAWAL);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 회원 없음")
    void withdrawMember_Fail_NotFound() {
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.withdrawMember(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- 소셜 로그인 (Social Signup) ---

    @Test
    @DisplayName("소셜 회원가입 성공")
    void socialSignupMember() {
        AddressCreateRequest addressRequest = new AddressCreateRequest("12345", "도로명", "상세", "집");
        SocialSignupRequest request = new SocialSignupRequest(
                "social@nhn.com", "소셜유저", LocalDate.of(1995, 5, 5), "010-5555-5555", "oauth123", addressRequest
        );

        given(memberRepository.existsByMemberEmail(request.memberEmail())).willReturn(false);
        given(gradeRepository.findByGradeName(GradeName.COMMON)).willReturn(Optional.of(new Grade(1L, GradeName.COMMON, BigDecimal.ZERO, 0)));

        memberService.socialSignupMember(request);

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("소셜 회원가입 실패 - 이미 존재하는 이메일")
    void socialSignupMember_Fail_EmailExists() {
        SocialSignupRequest request = new SocialSignupRequest("exist@nhn.com", "name", LocalDate.now(), "010-1111-1111", "id", null);
        given(memberRepository.existsByMemberEmail(request.memberEmail())).willReturn(true);

        assertThatThrownBy(() -> memberService.socialSignupMember(request))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    // --- 소셜 추가 정보 수정 (UpdateSocialInfo) ---

    @Test
    @DisplayName("소셜 정보 수정 성공 (주소 포함)")
    void updateSocialInfo_WithAddress() {
        Member member = createMember(1L, "social@nhn.com");
        AddressCreateRequest addrReq = new AddressCreateRequest("111", "base", "detail", "home");
        SocialSignupRequest request = new SocialSignupRequest(
                "social@nhn.com", "새이름", LocalDate.now(), "010-7777-7777", "oid", addrReq
        );

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.findByMemberContact(request.memberContact())).willReturn(Optional.empty());

        memberService.updateSocialInfo(1L, request);

        assertThat(member.getMemberName()).isEqualTo("새이름");
        verify(addressRepository).save(any(Address.class));
        verify(pointHistoryService).awardSignupPoints(1L);
    }

    @Test
    @DisplayName("소셜 정보 수정 실패 - 타인이 사용하는 연락처")
    void updateSocialInfo_Fail_DuplicateContact() {
        Member member = createMember(1L, "social@nhn.com");
        Member otherMember = createMember(2L, "other@nhn.com");
        SocialSignupRequest request = new SocialSignupRequest(
                "social@nhn.com", "name", LocalDate.now(), "010-7777-7777", "oid", null
        );

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.findByMemberContact(request.memberContact())).willReturn(Optional.of(otherMember));

        assertThatThrownBy(() -> memberService.updateSocialInfo(1L, request))
                .isInstanceOf(DuplicateMemberContactException.class);
    }

    // --- 비밀번호 재설정 (ResetPassword) ---

    @Test
    @DisplayName("비밀번호 재설정 성공")
    void resetPassword() {
        PasswordResetRequest request = new PasswordResetRequest("test@nhn.com", "123456", "newPassword");
        Member member = createMember(1L, "test@nhn.com");

        given(memberRepository.findByMemberEmail(request.memberEmail())).willReturn(Optional.of(member));
        given(emailService.verifyCode(request.memberEmail(), request.verificationCode())).willReturn(true);
        given(passwordEncoder.encode(request.newPassword())).willReturn("encodedNewPassword");

        memberService.resetPassword(request);

        assertThat(member.getMemberPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    @DisplayName("비밀번호 재설정 실패 - 회원 없음")
    void resetPassword_Fail_NotFound() {
        PasswordResetRequest request = new PasswordResetRequest("none@nhn.com", "123456", "pwd");
        given(memberRepository.findByMemberEmail(request.memberEmail())).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.resetPassword(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("비밀번호 재설정 실패 - 인증코드 불일치")
    void resetPassword_Fail_InvalidCode() {
        PasswordResetRequest request = new PasswordResetRequest("test@nhn.com", "wrong", "pwd");
        Member member = createMember(1L, "test@nhn.com");

        given(memberRepository.findByMemberEmail(request.memberEmail())).willReturn(Optional.of(member));
        given(emailService.verifyCode(request.memberEmail(), request.verificationCode())).willReturn(false);

        assertThatThrownBy(() -> memberService.resetPassword(request))
                .isInstanceOf(InvalidVerificationCodeException.class);
    }

    // --- 아이디 찾기 (FindMemberEmail) ---

    @Test
    @DisplayName("아이디 찾기 성공 - 긴 이메일 마스킹")
    void findMemberEmail_Long() {
        FindMemberIdRequest request = new FindMemberIdRequest("테스터", "010-1234-5678");
        Member member = createMember(1L, "abcdefg@nhn.com");

        given(memberRepository.findByMemberNameAndMemberContact(request.memberName(), request.memberContact()))
                .willReturn(Optional.of(member));

        String result = memberService.findMemberEmail(request);
        assertThat(result).isEqualTo("ab****@nhn.com");
    }

    @Test
    @DisplayName("아이디 찾기 성공 - 짧은 이메일 마스킹")
    void findMemberEmail_Short() {
        FindMemberIdRequest request = new FindMemberIdRequest("테스터", "010-1234-5678");
        Member member = createMember(1L, "ab@nhn.com"); // @앞이 2글자

        given(memberRepository.findByMemberNameAndMemberContact(request.memberName(), request.memberContact()))
                .willReturn(Optional.of(member));

        String result = memberService.findMemberEmail(request);
        // 로직: email.replaceAll("(?<=.{1}).(?=.*@)", "*") => 첫글자 빼고 @전까지 마스킹
        // a*@nhn.com
        assertThat(result).contains("*");
        assertThat(result).startsWith("a");
        assertThat(result).endsWith("@nhn.com");
    }

    @Test
    @DisplayName("아이디 찾기 실패 - 회원 없음")
    void findMemberEmail_Fail_NotFound() {
        FindMemberIdRequest request = new FindMemberIdRequest("테스터", "010-1234-5678");
        given(memberRepository.findByMemberNameAndMemberContact(anyString(), anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.findMemberEmail(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- 인증번호 발송 관련 ---

    @Test
    @DisplayName("회원가입 인증메일 발송 성공")
    void sendSignupVerificationCode_Success() {
        String email = "new@nhn.com";
        given(memberRepository.existsByMemberEmail(email)).willReturn(false);

        memberService.sendSignupVerificationCode(email);

        verify(emailService).sendVerificationCode(email);
    }

    @Test
    @DisplayName("회원가입 인증메일 발송 실패 - 이미 가입됨")
    void sendSignupVerificationCode_Fail_Duplicate() {
        String email = "exist@nhn.com";
        given(memberRepository.existsByMemberEmail(email)).willReturn(true);

        assertThatThrownBy(() -> memberService.sendSignupVerificationCode(email))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("비밀번호 재설정 인증메일 발송 성공")
    void sendResetPasswordVerificationCode_Success() {
        String email = "exist@nhn.com";
        given(memberRepository.existsByMemberEmail(email)).willReturn(true);

        memberService.sendResetPasswordVerificationCode(email);

        verify(emailService).sendVerificationCode(email);
    }

    @Test
    @DisplayName("비밀번호 재설정 인증메일 발송 실패 - 가입안됨")
    void sendResetPasswordVerificationCode_Fail_NotFound() {
        String email = "none@nhn.com";
        given(memberRepository.existsByMemberEmail(email)).willReturn(false);

        assertThatThrownBy(() -> memberService.sendResetPasswordVerificationCode(email))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- 휴면 해제 (Dormant Release) ---

    @Test
    @DisplayName("휴면 해제 요청 성공")
    void requestDormantRelease() {
        String email = "dormant@nhn.com";
        Member member = createMember(1L, email);
        member.setMemberState(MemberState.DORMANT);

        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.of(member));

        memberService.requestDormantRelease(email, "hookUrl");

        verify(doorayService).sendDormantVerificationCode(email, "hookUrl");
    }

    @Test
    @DisplayName("휴면 해제 요청 실패 - 휴면 계정 아님")
    void requestDormantRelease_Fail_NotDormant() {
        String email = "active@nhn.com";
        Member member = createMember(1L, email); // ACTIVE

        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.requestDormantRelease(email, "hook"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("휴면 해제 처리 성공")
    void processDormantRelease() {
        String email = "dormant@nhn.com";
        Member member = createMember(1L, email);
        member.setMemberState(MemberState.DORMANT);

        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.of(member));
        given(doorayService.verifyDormantCode(email, "123456")).willReturn(true);

        memberService.processDormantRelease(email, "123456");

        assertThat(member.getMemberState()).isEqualTo(MemberState.ACTIVE);
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("휴면 해제 처리 실패 - 코드 불일치")
    void processDormantRelease_Fail_InvalidCode() {
        String email = "dormant@nhn.com";
        Member member = createMember(1L, email);

        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.of(member));
        given(doorayService.verifyDormantCode(email, "wrong")).willReturn(false);

        assertThatThrownBy(() -> memberService.processDormantRelease(email, "wrong"))
                .isInstanceOf(InvalidVerificationCodeException.class);
    }

    // --- 기타 관리자 및 OAuth ---

    @Test
    @DisplayName("OAuth ID로 회원 조회")
    void getMemberByOauthId() {
        Member member = createMember(1L, "oauth@nhn.com");
        given(memberRepository.findByMemberOauthId("oauth123")).willReturn(Optional.of(member));

        MemberResponse response = memberService.getMemberByOauthId("oauth123");
        assertThat(response.memberId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("관리자 회원 목록 조회")
    void getMembersByAdmin() {
        Member member = createMember(1L, "test@nhn.com");
        Page<Member> page = new PageImpl<>(Collections.singletonList(member));
        given(memberRepository.findAll(any(Pageable.class))).willReturn(page);

        Page<MemberAdminResponse> result = memberService.getMembersByAdmin(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("관리자 회원 상태/등급 수정 성공")
    void updateMemberByAdmin() {
        Member member = createMember(1L, "test@nhn.com");
        Grade goldGrade = new Grade(2L, GradeName.GOLD, BigDecimal.valueOf(0.03), 100);

        MemberAdminUpdateRequest request = new MemberAdminUpdateRequest(1L, "WITHDRAWAL", "GOLD");

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(gradeRepository.findByGradeName(GradeName.GOLD)).willReturn(Optional.of(goldGrade));

        memberService.updateMemberByAdmin(request);

        assertThat(member.getMemberState()).isEqualTo(MemberState.WITHDRAWAL);
        assertThat(member.getGrade().getGradeName()).isEqualTo(GradeName.GOLD);
    }

    @Test
    @DisplayName("관리자 수정 실패 - 유효하지 않은 Enum 값")
    void updateMemberByAdmin_Fail_InvalidEnum() {
        Member member = createMember(1L, "test@nhn.com");
        MemberAdminUpdateRequest request = new MemberAdminUpdateRequest(1L, "INVALID_STATE", null);

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.updateMemberByAdmin(request))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    @DisplayName("관리자 수정 실패 - 존재하지 않는 등급")
    void updateMemberByAdmin_Fail_GradeNotFound() {
        Member member = createMember(1L, "test@nhn.com");
        MemberAdminUpdateRequest request = new MemberAdminUpdateRequest(1L, null, "ROYAL");

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        // Enum 파싱은 성공하나 DB에 없다고 가정
        given(gradeRepository.findByGradeName(GradeName.ROYAL)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.updateMemberByAdmin(request))
                .isInstanceOf(GradeNotFoundException.class);
    }
}