package com.nhnacademy.memberapi.client.order.service;

import com.nhnacademy.memberapi.client.order.dto.PointAccumulateRequest;
import com.nhnacademy.memberapi.client.order.dto.PointUsageRequest;
import com.nhnacademy.memberapi.client.order.saga.domain.OrderPointSagaLog;
import com.nhnacademy.memberapi.client.order.saga.domain.OrderPointSagaLogId;
import com.nhnacademy.memberapi.client.order.saga.domain.OrderSagaType;
import com.nhnacademy.memberapi.client.order.saga.repository.OrderPointSagaRepository;
import com.nhnacademy.memberapi.domain.point.service.PointHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPointServiceTest {

    @InjectMocks
    private OrderPointService orderPointService;

    @Mock
    private OrderPointSagaRepository orderPointSagaRepository;

    @Mock
    private PointHistoryService pointHistoryService;

    // 공통 변수
    private final UUID sagaId = UUID.randomUUID();
    private final Long memberId = 1L;
    private final Long orderId = 100L;
    private final int point = 5000;

    @Test
    @DisplayName("포인트 사용 - 정상 처리 (최초 요청)")
    void usePoint_Success() {
        PointUsageRequest request = new PointUsageRequest(memberId, orderId, point);
        OrderPointSagaLogId logId = new OrderPointSagaLogId(sagaId, OrderSagaType.USE_POINT);

        // given: 아직 처리되지 않음
        given(orderPointSagaRepository.existsById(logId)).willReturn(false);

        // when
        orderPointService.usePoint(sagaId, request);

        // then
        verify(pointHistoryService).usePoints(memberId, orderId, point);
        verify(orderPointSagaRepository).save(any(OrderPointSagaLog.class));
    }

    @Test
    @DisplayName("포인트 사용 - 멱등성 (이미 처리됨)")
    void usePoint_AlreadyProcessed() {
        PointUsageRequest request = new PointUsageRequest(memberId, orderId, point);
        OrderPointSagaLogId logId = new OrderPointSagaLogId(sagaId, OrderSagaType.USE_POINT);

        // given: 이미 처리됨
        given(orderPointSagaRepository.existsById(logId)).willReturn(true);

        // when
        orderPointService.usePoint(sagaId, request);

        // then: 로직 수행 X, 저장 X
        verify(pointHistoryService, never()).usePoints(anyLong(), anyLong(), anyInt());
        verify(orderPointSagaRepository, never()).save(any(OrderPointSagaLog.class));
    }

    @Test
    @DisplayName("포인트 환불 - 정상 처리")
    void refundPoint_Success() {
        PointUsageRequest request = new PointUsageRequest(memberId, orderId, point);
        OrderPointSagaLogId logId = new OrderPointSagaLogId(sagaId, OrderSagaType.REFUND_POINT);

        given(orderPointSagaRepository.existsById(logId)).willReturn(false);

        orderPointService.refundPoint(sagaId, request);

        verify(pointHistoryService).refundPurchasePoints(memberId, orderId, point);
        verify(orderPointSagaRepository).save(any(OrderPointSagaLog.class));
    }

    @Test
    @DisplayName("포인트 환불 - 멱등성 (이미 처리됨)")
    void refundPoint_AlreadyProcessed() {
        PointUsageRequest request = new PointUsageRequest(memberId, orderId, point);
        OrderPointSagaLogId logId = new OrderPointSagaLogId(sagaId, OrderSagaType.REFUND_POINT);

        given(orderPointSagaRepository.existsById(logId)).willReturn(true);

        orderPointService.refundPoint(sagaId, request);

        verify(pointHistoryService, never()).refundPurchasePoints(anyLong(), anyLong(), anyInt());
        verify(orderPointSagaRepository, never()).save(any(OrderPointSagaLog.class));
    }

    @Test
    @DisplayName("포인트 롤백 - 정상 처리 (USE_POINT 기록 있음)")
    void rollbackPoint_Success() {
        PointUsageRequest request = new PointUsageRequest(memberId, orderId, point);
        OrderPointSagaLogId rollbackLogId = new OrderPointSagaLogId(sagaId, OrderSagaType.ROLLBACK_POINT);
        OrderPointSagaLogId useLogId = new OrderPointSagaLogId(sagaId, OrderSagaType.USE_POINT);

        // given
        // 1. 롤백은 아직 안함
        given(orderPointSagaRepository.existsById(rollbackLogId)).willReturn(false);
        // 2. 사용(USE_POINT) 기록은 있음 -> 롤백 대상
        given(orderPointSagaRepository.existsById(useLogId)).willReturn(true);

        // when
        orderPointService.rollbackPoint(sagaId, request);

        // then
        verify(pointHistoryService).refundPurchasePoints(memberId, orderId, point);
        // 주의: 제공해주신 서비스 코드에서 rollbackPoint 메서드의 save() 부분이 주석 처리 되어있으므로
        // verify(orderPointSagaRepository).save(...)는 호출되지 않아야 합니다.
        // 만약 주석을 해제하신다면 verify를 추가해야 합니다.
    }

    @Test
    @DisplayName("포인트 롤백 - 멱등성 (이미 롤백됨)")
    void rollbackPoint_AlreadyProcessed() {
        PointUsageRequest request = new PointUsageRequest(memberId, orderId, point);
        OrderPointSagaLogId rollbackLogId = new OrderPointSagaLogId(sagaId, OrderSagaType.ROLLBACK_POINT);

        given(orderPointSagaRepository.existsById(rollbackLogId)).willReturn(true);

        orderPointService.rollbackPoint(sagaId, request);

        verify(pointHistoryService, never()).refundPurchasePoints(anyLong(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("포인트 롤백 - 실패 (USE_POINT 기록 없음)")
    void rollbackPoint_NoUsageLog() {
        PointUsageRequest request = new PointUsageRequest(memberId, orderId, point);
        OrderPointSagaLogId rollbackLogId = new OrderPointSagaLogId(sagaId, OrderSagaType.ROLLBACK_POINT);
        OrderPointSagaLogId useLogId = new OrderPointSagaLogId(sagaId, OrderSagaType.USE_POINT);

        // given
        given(orderPointSagaRepository.existsById(rollbackLogId)).willReturn(false);
        // 사용 기록이 없음 -> 포인트 차감된 적 없으니 롤백도 안함
        given(orderPointSagaRepository.existsById(useLogId)).willReturn(false);

        orderPointService.rollbackPoint(sagaId, request);

        verify(pointHistoryService, never()).refundPurchasePoints(anyLong(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("포인트 적립(구매확정) - 정상 처리")
    void accumulatePoint_Success() {
        int purchaseAmount = 10000;
        PointAccumulateRequest request = new PointAccumulateRequest(memberId, orderId, purchaseAmount);
        OrderPointSagaLogId logId = new OrderPointSagaLogId(sagaId, OrderSagaType.ACCUMULATE_POINT);

        given(orderPointSagaRepository.existsById(logId)).willReturn(false);

        orderPointService.accumulatePoint(sagaId, request);

        verify(pointHistoryService).awardPurchasePoints(memberId, orderId, purchaseAmount);
        verify(orderPointSagaRepository).save(any(OrderPointSagaLog.class));
    }

    @Test
    @DisplayName("포인트 적립 - 멱등성 (이미 적립됨)")
    void accumulatePoint_AlreadyProcessed() {
        PointAccumulateRequest request = new PointAccumulateRequest(memberId, orderId, 10000);
        OrderPointSagaLogId logId = new OrderPointSagaLogId(sagaId, OrderSagaType.ACCUMULATE_POINT);

        given(orderPointSagaRepository.existsById(logId)).willReturn(true);

        orderPointService.accumulatePoint(sagaId, request);

        verify(pointHistoryService, never()).awardPurchasePoints(anyLong(), anyLong(), anyInt());
        verify(orderPointSagaRepository, never()).save(any(OrderPointSagaLog.class));
    }
}