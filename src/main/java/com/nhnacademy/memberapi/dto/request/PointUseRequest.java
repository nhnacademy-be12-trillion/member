package com.nhnacademy.memberapi.dto.request;

// 포인트 사용
public record PointUseRequest(
        Long orderId,   // 어떤 주문에서 쓰는지
        Integer amount  // 얼마 쓸 건지
) {}
