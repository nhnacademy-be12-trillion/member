package com.nhnacademy.memberapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 포인트 정책
@Entity
@Table(name = "PointPolicy")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_policy_id")
    private Long pointPolicyId;

    @Column(name = "point_policy_code", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private PointPolicyCode pointPolicyCode;

    @Column(name = "point_policy_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PointPolicyType pointPolicyType;

    @Column(name = "point_policy_name", nullable = false)
    private String pointPolicyName;

    // pointPolicyName: RATE (도서 구매 적립률)
    @Column(name = "point_policy_rate")
    private BigDecimal pointPolicyRate;

    // pointPolicyName: AMOUNT (회원가입, 리뷰 작성 고정 금액)
    @Column(name = "point_policy_fixed_amount")
    private Integer pointPolicyFixedAmount;

    // 포인트 정책 수정일 (관리자)
    @Column(name = "last_modified_at", nullable = false)
    private LocalDateTime lastModifiedAt;

    public void updatePolicy(String name, PointPolicyType type, BigDecimal rate, Integer fixedAmount) {
        this.pointPolicyName = name;
        this.pointPolicyType = type;
        this.lastModifiedAt = LocalDateTime.now();

        // 타입에 따라 필드 설정
        if (type == PointPolicyType.RATE) {
            this.pointPolicyRate = rate;
            this.pointPolicyFixedAmount = null;
        } else if (type == PointPolicyType.AMOUNT) {
            this.pointPolicyFixedAmount = fixedAmount;
            this.pointPolicyRate = null;
        } else {
            throw new IllegalArgumentException("Invalid policy type during update.");
        }
    }
}