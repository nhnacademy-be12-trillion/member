package com.nhnacademy.memberapi.domain.member.event;

import com.nhnacademy.memberapi.domain.member.client.CouponClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberSignupEventListenerTest {

    @InjectMocks
    private MemberSignupEventListener memberSignupEventListener;

    @Mock
    private CouponClient couponClient;

    @Test
    @DisplayName("회원가입 이벤트 수신 시 웰컴 쿠폰 발급 요청 성공")
    void handleMemberSignup_Success() {
        Long memberId = 1L;
        MemberSignedUpEvent event = new MemberSignedUpEvent(memberId);

        memberSignupEventListener.handleMemberSignup(event);

        verify(couponClient, times(1)).issueWelcomeCoupon(memberId);
    }

    @Test
    @DisplayName("웰컴 쿠폰 발급 요청 실패 시 예외 처리 확인")
    void handleMemberSignup_Fail() {
        Long memberId = 1L;
        MemberSignedUpEvent event = new MemberSignedUpEvent(memberId);

        doThrow(new RuntimeException("Feign Client Error"))
                .when(couponClient).issueWelcomeCoupon(memberId);

        memberSignupEventListener.handleMemberSignup(event);

        verify(couponClient, times(1)).issueWelcomeCoupon(memberId);
    }
}