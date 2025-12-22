package com.nhnacademy.memberapi.client.order.dto;

public record PointUsageRequest(
    Long memberId,
    Long orderId,
    int point
) {}
