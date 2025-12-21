package com.nhnacademy.memberapi.client.order.saga.domain;

public enum OrderSagaType {
    USE_POINT,          // 포인트 사용 (주문 생성)
    REFUND_POINT,       // 포인트 증가 (주문 취소 / 주문 상품 환불)
    ROLLBACK_POINT,     // 포인트 복구 (주문 생성 실패)
    ACCUMULATE_POINT    // 포인트 적립 (구매 확정)
}
