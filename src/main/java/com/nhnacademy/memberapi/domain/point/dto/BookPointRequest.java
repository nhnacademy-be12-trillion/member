package com.nhnacademy.memberapi.domain.point.dto;

// 도서 구매 적립
public record BookPointRequest(
        Long orderId,
        Integer amount  // 구매 금액
) {}