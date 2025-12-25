package com.nhnacademy.memberapi.domain.member.event;

import com.nhnacademy.memberapi.domain.member.client.CouponClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSignupEventListener {

    private final CouponClient couponClient;

    // 커밋이 끝난 후에 실행
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberSignup(MemberSignedUpEvent event) {
        try {
            log.info("회원가입 완료 후 웰컴 쿠폰 발급 요청: memberId={}", event.memberId());
            couponClient.issueWelcomeCoupon(event.memberId());
        } catch (Exception e) {
            // 이미 커밋은 끝났으므로 여기서 실패해도 회원가입은 유지
            log.error("웰컴 쿠폰 발급 실패 (재시도 필요): memberId={}", event.memberId(), e);
        }
    }
}