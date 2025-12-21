package com.nhnacademy.memberapi.client.order.service;

import com.nhnacademy.memberapi.client.order.dto.PointAccumulateRequest;
import com.nhnacademy.memberapi.client.order.dto.PointUsageRequest;
import com.nhnacademy.memberapi.client.order.saga.domain.OrderPointSagaLog;
import com.nhnacademy.memberapi.client.order.saga.domain.OrderPointSagaLogId;
import com.nhnacademy.memberapi.client.order.saga.domain.OrderSagaType;
import com.nhnacademy.memberapi.client.order.saga.repository.OrderPointSagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderPointService {
    private final OrderPointSagaRepository orderPointSagaRepository;

    // 포인트 사용 (주문 생성)
    @Transactional
    public void usePoint(UUID sagaId, PointUsageRequest request) {
        OrderPointSagaLogId sagaLogId = new OrderPointSagaLogId(sagaId, OrderSagaType.USE_POINT);
        OrderPointSagaLog sagaLog = new OrderPointSagaLog(sagaLogId);

        Long memberId = request.memberId();
        Long orderId = request.orderId();
        int amount = request.point();

        // 1. 이미 처리된 작업이라면 즉시 리턴
        if (orderPointSagaRepository.existsById(sagaLogId)) {
            return;
        }

        // 2. 포인트 사용
        // TODO: 포인트 사용

        // 3. 기록을 DB에 남겨 멱등성 보장
        orderPointSagaRepository.save(sagaLog);
    }

    // 포인트 증가 (주문 취소, 주문 상품 환불)
    @Transactional
    public void refundPoint(UUID sagaId, PointUsageRequest request) {
        OrderPointSagaLogId sagaLogId = new OrderPointSagaLogId(sagaId, OrderSagaType.REFUND_POINT);
        OrderPointSagaLog sagaLog = new OrderPointSagaLog(sagaLogId);

        Long memberId = request.memberId();
        Long orderId = request.orderId();
        int amount = request.point();

        // 1. 이미 처리된 작업이라면 즉시 리턴
        if (orderPointSagaRepository.existsById(sagaLogId)) {
            return;
        }

        // 2. 포인트 증가
        // TODO: 포인트 환불

        // 3. 기록을 DB에 남겨 멱등성 보장
        orderPointSagaRepository.save(sagaLog);
    }

    // 포인트 복구 (주문 생성 실패)
    @Transactional
    public void rollbackPoint(UUID sagaId, PointUsageRequest request) {
        OrderPointSagaLogId sagaLogId = new OrderPointSagaLogId(sagaId, OrderSagaType.ROLLBACK_POINT);
        OrderPointSagaLog sagaLog = new OrderPointSagaLog(sagaLogId);

        Long memberId = request.memberId();
        Long orderId = request.orderId();
        int amount = request.point();

        // 1. 이미 처리된 작업이라면 즉시 리턴
        if (orderPointSagaRepository.existsById(sagaLogId)) {
            return;
        }

        // 2. 동일한 사가에 의해 포인트가 감소된 적이 있는지 확인
        boolean hasDecreased = orderPointSagaRepository.existsById(new OrderPointSagaLogId(sagaId, OrderSagaType.USE_POINT));

        // 2-1. 포인트가 감소된 적이 없다면 즉시 리턴 (포인트가 감소되지 않았으므로 증가하면 안 됨)
        if (!hasDecreased) {
            return;
        }

        // 3 포인트 증가
        // TODO: 포인트 복구

        // 4. 기록을 DB에 남겨 멱등성 보장
        orderPointSagaRepository.save(sagaLog);
    }

    @Transactional
    public void accumulatePoint(UUID sagaId, PointAccumulateRequest request) {
        OrderPointSagaLogId sagaLogId = new OrderPointSagaLogId(sagaId, OrderSagaType.ACCUMULATE_POINT);
        OrderPointSagaLog sagaLog = new OrderPointSagaLog(sagaLogId);

        Long memberId = request.memberId();
        Long orderId = request.orderId();
        int purchaseAmount = request.purchaseAmount(); // 주문 상품 가격

        // 1. 이미 처리된 작업이라면 즉시 리턴
        if (orderPointSagaRepository.existsById(sagaLogId)) {
            return;
        }

        // 2. 포인트 적립
        // TODO: 주문 상품의 구매 확정에 대해 포인트 적립

        // 3. 기록을 DB에 남겨 멱등성 보장
        orderPointSagaRepository.save(sagaLog);
    }
}
