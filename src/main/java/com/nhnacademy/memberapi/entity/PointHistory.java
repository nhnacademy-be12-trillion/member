package com.nhnacademy.memberapi.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 포인트 내역
@Entity
@Table(name = "PointHistory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_history_id")
    private Long pointHistoryId;

    @Column(name = "point_history_reason", nullable = false, length = 100)
    private String pointHistoryReason;

    @Column(name = "point_history_point", nullable = false)
    private Integer pointHistoryPoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // nullable = true
    // 리뷰 ID: 리뷰 적립 시 필요
    @Column(name = "review_id")
    private Long reviewId;

    // nullable = true
    // 주문 ID: 주문/결제 및 구매 적립 시 필요
    // 주문 서비스는 다른 도메인이므로, JPA 연관관계를 맺지 않고 ID 값만 저장한다.
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "current_total_point", nullable = false)
    private Integer currentTotalPoint;

    // 거래 발생 시각
    @Column(name = "transaction_at", nullable = false)
    private LocalDateTime transactionAt;

    @Builder
    public PointHistory(String pointHistoryReason, Integer pointHistoryPoint, Member member, Long reviewId, Long orderId, Integer currentTotalPoint, LocalDateTime transactionAt) {
        this.pointHistoryReason = pointHistoryReason;
        this.pointHistoryPoint = pointHistoryPoint;
        this.member = member;
        this.reviewId = reviewId;
        this.orderId = orderId;
        this.currentTotalPoint = currentTotalPoint;
        this.transactionAt = transactionAt;
    }
}