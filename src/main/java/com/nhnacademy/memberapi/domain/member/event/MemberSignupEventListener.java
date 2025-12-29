package com.nhnacademy.memberapi.domain.member.event;

import com.nhnacademy.memberapi.domain.member.client.CouponClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

// 웰컴 쿠폰 발급
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSignupEventListener {

    private final CouponClient couponClient;

    @Async
    @EventListener
    public void handleMemberSignup(MemberSignedUpEvent event) {
        try {
            log.info("회원가입 완료 후 웰컴 쿠폰 발급 요청: memberId={}", event.memberId());
            couponClient.issueWelcomeCoupon(event.memberId());
        } catch (Exception e) {
            log.error("웰컴 쿠폰 발급 실패 (재시도 필요): memberId={}", event.memberId(), e);
        }
    }
}