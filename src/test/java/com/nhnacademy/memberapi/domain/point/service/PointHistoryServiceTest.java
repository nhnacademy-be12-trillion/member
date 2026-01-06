package com.nhnacademy.memberapi.domain.point.service;

import com.nhnacademy.memberapi.domain.grade.entity.Grade;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import com.nhnacademy.memberapi.domain.point.dto.PointHistoryResponse;
import com.nhnacademy.memberapi.domain.point.dto.ReviewPointRequest;
import com.nhnacademy.memberapi.domain.point.entity.PointHistory;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicy;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicyCode;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicyType;
import com.nhnacademy.memberapi.domain.point.repository.PointHistoryRepository;
import com.nhnacademy.memberapi.global.error.exception.DuplicatePointException;
import com.nhnacademy.memberapi.global.error.exception.InsufficientPointsException;
import com.nhnacademy.memberapi.global.error.exception.PointHistoryNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceTest {

    @InjectMocks
    private PointHistoryService pointHistoryService;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointPolicyService pointPolicyService;

    private Member createMember(Long id, int points, Grade grade) {
        return Member.builder()
                .memberId(id)
                .memberEmail("test@test.com")
                .memberPassword("password")
                .memberName("Tester")
                .memberBirth(LocalDate.of(1990, 1, 1))
                .memberState(MemberState.ACTIVE)
                .memberRole(MemberRole.MEMBER)
                .memberPoint(points)
                .memberAccumulateAmount(0)
                .grade(grade)
                .build();
    }

    private PointPolicy createPolicy(PointPolicyCode code, PointPolicyType type, BigDecimal rate, Integer amount) {
        return PointPolicy.builder()
                .pointPolicyCode(code)
                .pointPolicyType(type)
                .pointPolicyName("Test Policy")
                .pointPolicyRate(rate)
                .pointPolicyFixedAmount(amount)
                .lastModifiedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("회원가입 포인트 적립")
    void awardSignupPoints() {
        Long memberId = 1L;
        Grade mockGrade = mock(Grade.class);
        Member member = createMember(memberId, 0, mockGrade);

        PointPolicy policy = createPolicy(PointPolicyCode.SIGNUP, PointPolicyType.AMOUNT, null, 5000);

        given(pointPolicyService.getPolicyByCode(PointPolicyCode.SIGNUP)).willReturn(policy);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        pointHistoryService.awardSignupPoints(memberId);

        assertThat(member.getMemberPoint()).isEqualTo(5000);
        verify(pointHistoryRepository, times(1)).save(any(PointHistory.class));
    }

    @Test
    @DisplayName("리뷰 포인트 적립 - 사진 없음(기본)")
    void awardReviewPoints_Base() {
        Long memberId = 1L;
        ReviewPointRequest request = new ReviewPointRequest(100L, false);
        Grade mockGrade = mock(Grade.class);
        Member member = createMember(memberId, 0, mockGrade);

        PointPolicy basePolicy = createPolicy(PointPolicyCode.REVIEW_BASE, PointPolicyType.AMOUNT, null, 200);

        given(pointHistoryRepository.existsByReviewId(request.reviewId())).willReturn(false);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(pointPolicyService.getPolicyByCode(PointPolicyCode.REVIEW_BASE)).willReturn(basePolicy);

        pointHistoryService.awardReviewPoints(memberId, request);

        assertThat(member.getMemberPoint()).isEqualTo(200);
        verify(pointHistoryRepository).save(any(PointHistory.class));
    }

    @Test
    @DisplayName("리뷰 포인트 적립 - 사진 포함(기본 + 보너스)")
    void awardReviewPoints_WithPhoto() {
        Long memberId = 1L;
        Grade mockGrade = mock(Grade.class);
        ReviewPointRequest request = new ReviewPointRequest(100L, true);
        Member member = createMember(memberId, 0, mockGrade);

        PointPolicy basePolicy = createPolicy(PointPolicyCode.REVIEW_BASE, PointPolicyType.AMOUNT, null, 200);
        PointPolicy photoPolicy = createPolicy(PointPolicyCode.REVIEW_PHOTO, PointPolicyType.AMOUNT, null, 300);

        given(pointHistoryRepository.existsByReviewId(request.reviewId())).willReturn(false);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(pointPolicyService.getPolicyByCode(PointPolicyCode.REVIEW_BASE)).willReturn(basePolicy);
        given(pointPolicyService.getPolicyByCode(PointPolicyCode.REVIEW_PHOTO)).willReturn(photoPolicy);

        pointHistoryService.awardReviewPoints(memberId, request);

        assertThat(member.getMemberPoint()).isEqualTo(500);
        verify(pointHistoryRepository).save(any(PointHistory.class));
    }

    @Test
    @DisplayName("리뷰 포인트 중복 적립 실패")
    void awardReviewPoints_Duplicate_Fail() {
        ReviewPointRequest request = new ReviewPointRequest(100L, false);
        given(pointHistoryRepository.existsByReviewId(request.reviewId())).willReturn(true);

        assertThatThrownBy(() -> pointHistoryService.awardReviewPoints(1L, request))
                .isInstanceOf(DuplicatePointException.class);
    }

    @Test
    @DisplayName("도서 구매 포인트 적립 (기본 1% + 등급 1%)")
    void awardPurchasePoints() {
        Long memberId = 1L;
        Long orderId = 999L;
        int paymentAmount = 10000;

        Grade mockGrade = mock(Grade.class);
        given(mockGrade.getGradePointRatio()).willReturn(new BigDecimal("0.01"));

        Member member = createMember(memberId, 0, mockGrade);

        PointPolicy bookPolicy = createPolicy(PointPolicyCode.PURCHASE, PointPolicyType.RATE, new BigDecimal("0.01"), null);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(pointPolicyService.getPolicyByCode(PointPolicyCode.PURCHASE)).willReturn(bookPolicy);

        pointHistoryService.awardPurchasePoints(memberId, orderId, paymentAmount);

        assertThat(member.getMemberPoint()).isEqualTo(200);
        verify(pointHistoryRepository, times(2)).save(any(PointHistory.class));
    }

    @Test
    @DisplayName("포인트 사용")
    void usePoints() {
        Long memberId = 1L;
        Grade mockGrade = mock(Grade.class);
        Member member = createMember(memberId, 1000, mockGrade);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        pointHistoryService.usePoints(memberId, 55L, 500);

        assertThat(member.getMemberPoint()).isEqualTo(500);
        verify(pointHistoryRepository).save(any(PointHistory.class));
    }

    @Test
    @DisplayName("포인트 사용 실패 - 잔액 부족")
    void usePoints_Insufficient_Fail() {
        Long memberId = 1L;
        Grade mockGrade = mock(Grade.class);
        Member member = createMember(memberId, 100, mockGrade);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> pointHistoryService.usePoints(memberId, 55L, 500))
                .isInstanceOf(InsufficientPointsException.class);
    }

    @Test
    @DisplayName("포인트 내역 조회")
    void getHistories() {
        Long memberId = 1L;
        Grade mockGrade = mock(Grade.class);
        Member member = createMember(memberId, 1000, mockGrade);

        PointHistory history = PointHistory.builder()
                .member(member)
                .pointHistoryReason("테스트")
                .pointHistoryPoint(100)
                .currentTotalPoint(1100)
                .transactionAt(LocalDateTime.now())
                .build();

        given(pointHistoryRepository.findAllByMember_MemberIdOrderByTransactionAtDesc(memberId))
                .willReturn(List.of(history));

        List<PointHistoryResponse> responses = pointHistoryService.getHistories(memberId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).amount()).isEqualTo(100);
    }

    @Test
    @DisplayName("포인트 환불 (주문 취소/반품)")
    void refundPurchasePoints() {
        Long memberId = 1L;
        Long orderId = 999L;
        int refundAmount = 5000;
        Grade mockGrade = mock(Grade.class);
        Member member = createMember(memberId, 1000, mockGrade);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(pointHistoryRepository.existsByMember_MemberIdAndOrderId(memberId, orderId)).willReturn(true);

        pointHistoryService.refundPurchasePoints(memberId, orderId, refundAmount);

        assertThat(member.getMemberPoint()).isEqualTo(6000);
        verify(pointHistoryRepository).save(any(PointHistory.class));
    }

    @Test
    @DisplayName("포인트 환불 실패 - 내역 없음")
    void refundPurchasePoints_NotFound() {
        Long memberId = 1L;
        Long orderId = 999L;
        int refundAmount = 5000;
        Grade mockGrade = mock(Grade.class);
        Member member = createMember(memberId, 1000, mockGrade);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(pointHistoryRepository.existsByMember_MemberIdAndOrderId(memberId, orderId)).willReturn(false);

        assertThatThrownBy(() -> pointHistoryService.refundPurchasePoints(memberId, orderId, refundAmount))
                .isInstanceOf(PointHistoryNotFoundException.class);
    }
}