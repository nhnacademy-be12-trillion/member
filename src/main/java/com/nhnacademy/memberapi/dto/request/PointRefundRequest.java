package com.nhnacademy.memberapi.dto.request;

import jakarta.validation.constraints.NotNull;

// 포인트 회수 요청 (주문 -> 회원)
public record PointRefundRequest(
        @NotNull
        Long memberId,
        @NotNull
        Long orderId
) {}