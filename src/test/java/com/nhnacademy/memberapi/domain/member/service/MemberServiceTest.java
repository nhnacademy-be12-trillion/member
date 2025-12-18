package com.nhnacademy.memberapi.domain.member.service;

import com.nhnacademy.memberapi.domain.address.dto.AddressCreateRequest;
import com.nhnacademy.memberapi.domain.address.entity.Address;
import com.nhnacademy.memberapi.domain.grade.entity.Grade;
import com.nhnacademy.memberapi.domain.grade.entity.GradeName;
import com.nhnacademy.memberapi.domain.grade.repository.GradeRepository;
import com.nhnacademy.memberapi.domain.member.dto.FindMemberIdRequest;
import com.nhnacademy.memberapi.domain.member.dto.MemberSignupRequest;
import com.nhnacademy.memberapi.domain.member.dto.MemberUpdateRequest;
import com.nhnacademy.memberapi.domain.member.dto.PasswordResetRequest;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import com.nhnacademy.memberapi.global.error.exception.InvalidVerificationCodeException;
import com.nhnacademy.memberapi.global.error.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private DoorayService doorayService;

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

    @Test
    @DisplayName("회원가입 - 주소 연관관계 및 저장 확인")
    void signupMember() {
        AddressCreateRequest addressRequest = new AddressCreateRequest("12345", "도로명", "상세", "집");
        MemberSignupRequest request = new MemberSignupRequest(
                "test@nhn.com", "123456", "password", "테스터",
                "010-1234-5678", LocalDate.of(1990, 1, 1), addressRequest
        );

        Grade defaultGrade = new Grade(1L, GradeName.COMMON, BigDecimal.valueOf(0.01), 0);

        given(memberRepository.existsByMemberEmail(request.memberEmail())).willReturn(false);
        given(emailService.verifyCode(request.memberEmail(), request.verificationCode())).willReturn(true);
        given(gradeRepository.findByGradeName(GradeName.COMMON)).willReturn(Optional.of(defaultGrade));
        given(passwordEncoder.encode(request.memberPassword())).willReturn("encodedPassword");

        memberService.signupMember(request);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getMemberEmail()).isEqualTo(request.memberEmail());
        assertThat(savedMember.getMemberPassword()).isEqualTo("encodedPassword");

        assertThat(savedMember.getAddresses()).hasSize(1);
        Address savedAddress = savedMember.getAddresses().get(0);
        assertThat(savedAddress.getMember()).isEqualTo(savedMember); // 양방향 매핑 확인
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void signupMember_Fail_EmailExists() {
        MemberSignupRequest request = new MemberSignupRequest(
                "duplicate@nhn.com", "123456", "pwd", "memberName", "010-1234-5678", LocalDate.now(), null
        );
        given(memberRepository.existsByMemberEmail(request.memberEmail())).willReturn(true);

        assertThatThrownBy(() -> memberService.signupMember(request))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("회원가입 실패 - 인증코드 불일치")
    void signupMember_Fail_InvalidCode() {
        MemberSignupRequest request = new MemberSignupRequest(
                "test@nhn.com", "wrongCode", "pwd", "memberName", "010-1234-5678", LocalDate.now(), null
        );
        given(memberRepository.existsByMemberEmail(request.memberEmail())).willReturn(false);
        given(emailService.verifyCode(request.memberEmail(), request.verificationCode())).willReturn(false);

        assertThatThrownBy(() -> memberService.signupMember(request))
                .isInstanceOf(InvalidVerificationCodeException.class);
    }

    @Test
    @DisplayName("회원 탈퇴")
    void withdrawMember() {
        // given
        Long memberId = 1L;
        Member member = createMember(memberId, "test@nhn.com");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        memberService.withdrawMember(memberId);

        // then
        assertThat(member.getMemberState()).isEqualTo(MemberState.WITHDRAWAL);
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void updateMember() {
        Long memberId = 1L;
        Member member = createMember(memberId, "test@nhn.com");
        MemberUpdateRequest request = new MemberUpdateRequest(
                "010-9999-9999", "새이름", LocalDate.of(2000, 1, 1)
        );

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        memberService.updateMember(memberId, request);

        // then (Entity Dirty Checking을 가정하므로 객체 상태 변화 확인)
        assertThat(member.getMemberContact()).isEqualTo("010-9999-9999");
        assertThat(member.getMemberName()).isEqualTo("새이름");
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("비밀번호 재설정")
    void resetPassword() {
        // given
        PasswordResetRequest request = new PasswordResetRequest("test@nhn.com", "123456", "newPassword");
        Member member = createMember(1L, "test@nhn.com");

        given(memberRepository.findByMemberEmail(request.memberEmail())).willReturn(Optional.of(member));
        given(emailService.verifyCode(request.memberEmail(), request.verificationCode())).willReturn(true);
        given(passwordEncoder.encode(request.newPassword())).willReturn("encodedNewPassword");

        memberService.resetPassword(request);

        assertThat(member.getMemberPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    @DisplayName("휴면 해제 요청 (인증번호 발송)")
    void requestDormantRelease() {
        String email = "dormant@nhn.com";
        String hookUrl = "http://dooray.com/hook";
        Member member = createMember(1L, email);
        member.setMemberState(MemberState.DORMANT);

        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.of(member));

        memberService.requestDormantRelease(email, hookUrl);

        verify(doorayService).sendDormantVerificationCode(email, hookUrl);
    }

    @Test
    @DisplayName("휴면 해제 요청 - 실패 (휴면 계정이 아님)")
    void requestDormantRelease_Fail_NotDormant() {
        String email = "active@nhn.com";
        Member member = createMember(1L, email);

        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.requestDormantRelease(email, "url"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("휴면 계정이 아닙니다.");
    }

    @Test
    @DisplayName("휴면 해제 처리 (상태 변경 확인)")
    void processDormantRelease() {
        String email = "dormant@nhn.com";
        String code = "123456";
        Member member = createMember(1L, email);
        member.setMemberState(MemberState.DORMANT);

        given(memberRepository.findByMemberEmail(email)).willReturn(Optional.of(member));
        given(doorayService.verifyDormantCode(email, code)).willReturn(true);

        memberService.processDormantRelease(email, code);

        assertThat(member.getMemberState()).isEqualTo(MemberState.ACTIVE);
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("아이디 찾기")
    void findMemberEmail() {
        FindMemberIdRequest request = new FindMemberIdRequest("테스터", "010-1234-5678");
        Member member = createMember(1L, "abcdefg@nhn.com");

        given(memberRepository.findByMemberNameAndMemberContact(request.memberName(), request.memberContact()))
                .willReturn(Optional.of(member));

        String result = memberService.findMemberEmail(request);

        assertThat(result).isNotNull();
        assertThat(result).contains("****"); // 마스킹이 적용되었는지 확인
        assertThat(result).contains("nhn.com"); // 도메인은 유지되는지 확인
    }

    @Test
    @DisplayName("회원가입용 이메일 인증코드 발송 - 실패 (중복 이메일)")
    void sendSignupVerificationCode_Fail_Duplicate() {
        String email = "exist@nhn.com";
        given(memberRepository.existsByMemberEmail(email)).willReturn(true);

        assertThatThrownBy(() -> memberService.sendSignupVerificationCode(email))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(emailService, never()).sendVerificationCode(anyString());
    }
}