package com.nhnacademy.memberapi.domain.point.dto;

import com.nhnacademy.memberapi.domain.point.entity.PointPolicy;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicyCode;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicyType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointPolicyResponse(
        Long pointPolicyId,
        PointPolicyCode pointPolicyCode,
        String pointPolicyName,
        PointPolicyType pointPolicyType,
        BigDecimal pointPolicyRate,
        Integer pointPolicyFixedAmount,
        LocalDateTime lastModifiedAt
) {
    public static PointPolicyResponse from(PointPolicy policy) {
        return new PointPolicyResponse(
                policy.getPointPolicyId(),
                policy.getPointPolicyCode(),
                policy.getPointPolicyName(),
                policy.getPointPolicyType(),
                policy.getPointPolicyRate(),
                policy.getPointPolicyFixedAmount(),
                policy.getLastModifiedAt()
        );
    }
}