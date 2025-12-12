package com.nhnacademy.memberapi.domain.member.repository;

import com.nhnacademy.memberapi.domain.grade.entity.Grade;
import com.nhnacademy.memberapi.domain.grade.entity.GradeName;
import com.nhnacademy.memberapi.domain.grade.repository.GradeRepository;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // JPA 관련 컴포넌트만 로드하여 빠르게 테스트 (Transactional 포함 -> 자동 롤백)
@TestPropertySource(properties = {
        "spring.config.import=optional:configserver:"
})
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private TestEntityManager entityManager; // 테스트용 엔티티 매니저 (Repository를 거치지 않고 DB 설정 가능)

    private Member savedMember;

    @BeforeEach
    void setUp() {
        Grade defaultGrade = Grade.builder()
                .gradeName(GradeName.COMMON)
                .gradePointRatio(BigDecimal.valueOf(0.01))
                .gradeCondition(0)
                .build();
        Grade savedGrade = gradeRepository.save(defaultGrade);

        Member member = Member.builder()
                .memberEmail("test@nhn.com")
                .memberPassword("password123!")
                .memberName("테스터")
                .memberContact("010-1234-5678")
                .memberBirth(LocalDate.of(1990, 1, 1))
                .memberState(MemberState.ACTIVE)
                .memberLastestLoginAt(LocalDate.now())
                .memberRole(MemberRole.MEMBER)
                .memberPoint(0)
                .memberAccumulateAmount(0)
                .grade(savedGrade)
                .build();

        savedMember = memberRepository.save(member);
    }

    @Test
    @DisplayName("이메일로 회원 존재 여부 확인 - 존재하는 경우")
    void existsByMemberEmail() {
        boolean exists = memberRepository.existsByMemberEmail("test@nhn.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일로 회원 존재 여부 확인 - 존재하지 않는 경우")
    void existsByMemberEmail_False() {
        boolean exists = memberRepository.existsByMemberEmail("unknown@nhn.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("이메일로 회원 조회")
    void findByMemberEmail() {
        Optional<Member> foundMember = memberRepository.findByMemberEmail("test@nhn.com");

        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getMemberName()).isEqualTo("테스터");
        assertThat(foundMember.get().getMemberEmail()).isEqualTo("test@nhn.com");
    }

    @Test
    @DisplayName("이메일로 회원 조회 - 실패 (존재하지 않음)")
    void findByMemberEmail_NotFound() {
        Optional<Member> foundMember = memberRepository.findByMemberEmail("ghost@nhn.com");

        assertThat(foundMember).isEmpty();
    }

    @Test
    @DisplayName("이름과 연락처로 회원 조회")
    void findByMemberNameAndMemberContact() {
        Optional<Member> foundMember = memberRepository.findByMemberNameAndMemberContact("테스터", "010-1234-5678");

        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getMemberEmail()).isEqualTo("test@nhn.com");
    }

    @Test
    @DisplayName("이름과 연락처로 회원 조회 - 실패 (이름 불일치)")
    void findByMemberNameAndMemberContact_Fail_NameMismatch() {
        // when
        Optional<Member> foundMember = memberRepository.findByMemberNameAndMemberContact("다른이름", "010-1234-5678");

        // then
        assertThat(foundMember).isEmpty();
    }

    @Test
    @DisplayName("이름과 연락처로 회원 조회 - 실패 (연락처 불일치)")
    void findByMemberNameAndMemberContact_Fail_ContactMismatch() {
        Optional<Member> foundMember = memberRepository.findByMemberNameAndMemberContact("테스터", "010-9876-5432");

        assertThat(foundMember).isEmpty();
    }

    @Test
    @DisplayName("회원 ID로 존재 여부 확인 - 존재하는 경우")
    void existsByMemberId() {
        boolean exists = memberRepository.existsByMemberId(savedMember.getMemberId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("회원 ID로 존재 여부 확인 - 존재하지 않는 경우")
    void existsByMemberId_False() {
        boolean exists = memberRepository.existsByMemberId(99999L); // 존재하지 않는 ID

        assertThat(exists).isFalse();
    }
}