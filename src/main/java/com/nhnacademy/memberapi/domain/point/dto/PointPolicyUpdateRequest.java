package com.nhnacademy.memberapi.domain.point.dto;

import com.nhnacademy.memberapi.domain.point.entity.PointPolicyType;
import java.math.BigDecimal;

public record PointPolicyUpdateRequest(
        String pointPolicyName,
        // 포인트 정책 타입(RATE, AMOUNT)
        PointPolicyType pointPolicyType,
        // 포인트 RATE
        BigDecimal pointPolicyRate,
        // 포인트 AMOUNT
        Integer pointPolicyFixedAmount
) {}