package com.nhnacademy.memberapi.domain.point.repository;

import com.nhnacademy.memberapi.domain.grade.entity.Grade;
import com.nhnacademy.memberapi.domain.grade.entity.GradeName;
import com.nhnacademy.memberapi.domain.grade.repository.GradeRepository;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import com.nhnacademy.memberapi.domain.point.entity.PointHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.config.import=optional:configserver:"
})
class PointHistoryRepositoryTest {

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GradeRepository gradeRepository;

    private Member createAndSaveMember() {
        Grade defaultGrade = Grade.builder()
                .gradeName(GradeName.COMMON)
                .gradePointRatio(BigDecimal.valueOf(0.01))
                .gradeCondition(0)
                .build();
        Grade savedGrade = gradeRepository.save(defaultGrade);

        Member member = Member.builder()
                .memberEmail("test@test.com")
                .memberPassword("1234")
                .memberName("테스터")
                .memberBirth(LocalDate.of(1990, 1, 1))
                .memberState(MemberState.ACTIVE)
                .memberLastestLoginAt(LocalDate.now())
                .memberRole(MemberRole.MEMBER)
                .memberPoint(0)
                .memberAccumulateAmount(0)
                .grade(savedGrade)
                .build();

        return memberRepository.save(member);
    }

    @Test
    @DisplayName("리뷰 ID로 포인트 지급 내역 존재 여부 확인")
    void existsByReviewId() {
        Member member = createAndSaveMember();
        Long reviewId = 100L;

        PointHistory history = PointHistory.builder()
                .member(member)
                .pointHistoryReason("리뷰 작성 적립")
                .pointHistoryPoint(500)
                .currentTotalPoint(500)
                .transactionAt(LocalDateTime.now())
                .reviewId(reviewId)
                .build();

        pointHistoryRepository.save(history);

        boolean exists = pointHistoryRepository.existsByReviewId(reviewId);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("리뷰 ID로 포인트 지급 내역 존재 여부 확인 - False")
    void existsByReviewId_False() {
        createAndSaveMember();

        boolean exists = pointHistoryRepository.existsByReviewId(999L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("특정 회원의 이력을 최신순(TransactionAt Desc)으로 조회")
    void findAllByMember_MemberIdOrderByTransactionAtDesc() {
        Member member = createAndSaveMember();

        PointHistory oldHistory = PointHistory.builder()
                .member(member)
                .pointHistoryReason("과거 적립")
                .pointHistoryPoint(100)
                .currentTotalPoint(100)
                .transactionAt(LocalDateTime.now().minusDays(5))
                .build();

        PointHistory recentHistory = PointHistory.builder()
                .member(member)
                .pointHistoryReason("최신 적립")
                .pointHistoryPoint(200)
                .currentTotalPoint(300)
                .transactionAt(LocalDateTime.now())
                .build();

        PointHistory middleHistory = PointHistory.builder()
                .member(member)
                .pointHistoryReason("중간 적립")
                .pointHistoryPoint(50)
                .currentTotalPoint(350)
                .transactionAt(LocalDateTime.now().minusDays(1))
                .build();

        pointHistoryRepository.saveAll(List.of(oldHistory, recentHistory, middleHistory));

        List<PointHistory> result = pointHistoryRepository.findAllByMember_MemberIdOrderByTransactionAtDesc(member.getMemberId());

        assertThat(result).hasSize(3);

        assertThat(result.get(0).getPointHistoryReason()).isEqualTo("최신 적립");
        assertThat(result.get(1).getPointHistoryReason()).isEqualTo("중간 적립");
        assertThat(result.get(2).getPointHistoryReason()).isEqualTo("과거 적립");
    }

    @Test
    @DisplayName("회원 ID와 주문 ID로 이력 조회")
    void findAllByMember_MemberIdAndOrderId() {
        Member member = createAndSaveMember();
        Long targetOrderId = 777L;

        PointHistory targetHistory = PointHistory.builder()
                .member(member)
                .pointHistoryReason("주문 적립")
                .pointHistoryPoint(1000)
                .currentTotalPoint(1000)
                .transactionAt(LocalDateTime.now())
                .orderId(targetOrderId)
                .build();

        PointHistory otherHistory = PointHistory.builder()
                .member(member)
                .pointHistoryReason("다른 주문 적립")
                .pointHistoryPoint(500)
                .currentTotalPoint(1500)
                .transactionAt(LocalDateTime.now())
                .orderId(888L)
                .build();

        pointHistoryRepository.saveAll(List.of(targetHistory, otherHistory));

        List<PointHistory> result = pointHistoryRepository.findAllByMember_MemberIdAndOrderId(member.getMemberId(), targetOrderId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(targetOrderId);
        assertThat(result.get(0).getPointHistoryPoint()).isEqualTo(1000);
    }
}