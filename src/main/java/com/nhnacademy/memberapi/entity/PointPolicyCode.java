package com.nhnacademy.memberapi.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;

// 포인트 정책을 식별하기 위해 추가
@Getter
@RequiredArgsConstructor
public enum PointPolicyCode {
    PURCHASE("purchase", PointPolicyType.RATE, new BigDecimal("0.01"), null),
    SIGNUP("signup", PointPolicyType.AMOUNT, null, 5000),
    REVIEW_BASE("review_base", PointPolicyType.AMOUNT, null, 200),
    REVIEW_PHOTO("review_photo", PointPolicyType.AMOUNT, null, 300);

    private final String defaultName;
    private final PointPolicyType policyType;
    private final BigDecimal defaultRate;
    private final Integer defaultAmount;
}