package com.nhnacademy.memberapi.dto.request;

// 리뷰 포인트 적립
public record ReviewPointRequest(
        Long reviewId,
        boolean hasPhoto
) {}
