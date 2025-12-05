package com.nhnacademy.memberapi.service;

import com.nhnacademy.memberapi.dto.request.ReviewPointRequest;
import com.nhnacademy.memberapi.dto.response.PointHistoryResponse;
import com.nhnacademy.memberapi.entity.*;
import com.nhnacademy.memberapi.exception.DuplicatePointException;
import com.nhnacademy.memberapi.exception.MemberNotFoundException;
import com.nhnacademy.memberapi.exception.PointHistoryNotFoundException;
import com.nhnacademy.memberapi.repository.MemberRepository;
import com.nhnacademy.memberapi.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// 포인트 조회, 적립/사용
@Service
@RequiredArgsConstructor
@Transactional
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;
    private final MemberRepository memberRepository;
    private final PointPolicyService pointPolicyService;

    // 회원가입 적립 (회원)
    public void awardSignupPoints(Long memberId) {
        PointPolicy policy = pointPolicyService.getPolicyByCode(PointPolicyCode.SIGNUP);
        Member member = getMember(memberId);
        processPointTransaction(member, policy.getPointPolicyFixedAmount(), "회원가입 적립", null, null);
    }

    // 리뷰 작성 적립 (리뷰 -> 회원)
    public void awardReviewPoints(Long memberId, ReviewPointRequest request) {
        Long reviewId = request.reviewId();
        // 중복 적립 방지
        if (pointHistoryRepository.existsByReviewId(reviewId)) {
            throw new DuplicatePointException("이미 적립된 리뷰입니다.");
        }

        Member member = getMember(memberId);
        int totalAmount = 0;
        String reason = "리뷰 작성 적립(일반)";

        // 기본 리뷰 포인트 정책 조회
        PointPolicy basePolicy = pointPolicyService.getPolicyByCode(PointPolicyCode.REVIEW_BASE);

        totalAmount += basePolicy.getPointPolicyFixedAmount();

        // 사진이 포함된 리뷰면 보너스 포인트 지금
        if (request.hasPhoto()) {
            PointPolicy photoPolicy = pointPolicyService.getPolicyByCode(PointPolicyCode.REVIEW_PHOTO);

            totalAmount += photoPolicy.getPointPolicyFixedAmount();
            reason = "리뷰 작성 적립(사진 포함)";
        }

        if (totalAmount > 0) {
            processPointTransaction(member, totalAmount, reason, null, reviewId);
        }
    }

    // 도서 구매 적립 (주문 -> 회원)
    public void awardPurchasePoints(Long memberId, Long orderId, int paymentAmount) {
        Member member = getMember(memberId);

        // 적립액 계산: 결제금액 * 적립률
        // 도서 기본 적립 포인트
        PointPolicy policy = pointPolicyService.getPolicyByCode(PointPolicyCode.PURCHASE);
        int earnedBookPoints = policy.getPointPolicyRate()
                .multiply(new BigDecimal(paymentAmount))
                .intValue();
        // 등급별 포인트
        Grade grade = member.getGrade();
        int earnedGradePoints = grade.getGradePointRatio()
                .multiply(new BigDecimal(paymentAmount))
                .intValue();

        if (earnedBookPoints > 0) {
            processPointTransaction(member, earnedBookPoints, "도서 구매 적립(도서 적립금)", orderId, null);
        }
        if (earnedGradePoints > 0) {
            processPointTransaction(member, earnedGradePoints, "도서 구매 적립(등급별 적립금)", orderId, null);
        }
    }

    // 포인트 사용 (주문 -> 회원)
    public void usePoints(Long memberId, Long orderId, int amountToUse) {
        Member member = getMember(memberId);
        // 사용은 음수로 처리
        processPointTransaction(member, -amountToUse, "포인트 결제 사용", orderId, null);

        // 포인트 사용 시 잔액 부족 문제는 Member 엔티티에서 검증
    }

    // 포인트 이력 조회 (마이페이지)
    @Transactional(readOnly = true)
    public List<PointHistoryResponse> getHistories(Long memberId) {
        return pointHistoryRepository.findAllByMember_MemberIdOrderByTransactionAtDesc(memberId)
                .stream()
                .map(PointHistoryResponse::from)
                .collect(Collectors.toList());
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found: " + memberId));
    }

    // 회원 잔액 업데이트 + 이력 저장 (트랜잭션 처리)
    private void processPointTransaction(Member member, int amount, String reason, Long orderId, Long reviewId) {
        // Member 엔티티 상태 변경 (Dirty Checking)
        member.adjustPoint(amount);

        // 이력 생성
        PointHistory history = PointHistory.builder()
                .member(member)
                .pointHistoryReason(reason)
                .pointHistoryPoint(amount)
                .currentTotalPoint(member.getMemberPoint()) // 변경 후 잔액 저장
                .transactionAt(LocalDateTime.now())
                .orderId(orderId)
                .reviewId(reviewId)
                .build();

        pointHistoryRepository.save(history);
    }

    // 도서 반품 시 포인트 회수
    @Transactional
    public void refundPurchasePoints(Long memberId, Long orderId) {
        Member member = getMember(memberId);
        // 주문 ID(orderId)로 적립된 이력 찾기
        List<PointHistory> histories = pointHistoryRepository.findAllByMember_MemberIdAndOrderId(memberId, orderId);

        if (histories.isEmpty()) {
            throw new PointHistoryNotFoundException("해당 주문 ID로 적립된 포인트 내역을 찾을 수 없습니다.");
        }

        for (PointHistory history : histories) {
            // 이미 사용되거나 환불된 이력 제외
            if (history.getPointHistoryPoint() < 0) {
                continue;
            }
            // 적립된 포인트의 역방향 트랜잭션 금액을 계산해서 차감
            int amountToRefund = -history.getPointHistoryPoint();
            String refundReason = "도서 반품 포인트 회수: " + history.getPointHistoryReason();
            processPointTransaction(member, amountToRefund, refundReason, orderId, null);
        }
    }

}