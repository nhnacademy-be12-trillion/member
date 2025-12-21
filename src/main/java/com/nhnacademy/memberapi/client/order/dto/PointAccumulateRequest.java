package com.nhnacademy.memberapi.client.order.dto;

public record PointAccumulateRequest(
   Long memberId,
   Long orderId,
   int purchaseAmount
) {}
