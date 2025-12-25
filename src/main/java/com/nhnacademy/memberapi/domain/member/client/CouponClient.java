package com.nhnacademy.memberapi.domain.member.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "coupon-service")
public interface CouponClient {

    @PostMapping("/coupons/welcome")
    void issueWelcomeCoupon(@RequestHeader("X-Member-Id") Long memberId);
}