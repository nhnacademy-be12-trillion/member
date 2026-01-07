package com.nhnacademy.memberapi.client.order.controller;

import com.nhnacademy.memberapi.client.order.controller.docs.OrderPointControllerDocs;
import com.nhnacademy.memberapi.client.order.dto.PointAccumulateRequest;
import com.nhnacademy.memberapi.client.order.dto.PointUsageRequest;
import com.nhnacademy.memberapi.client.order.service.OrderPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderPointController implements OrderPointControllerDocs {
    private final OrderPointService orderPointService;

    @PatchMapping("/members/points/use")
    public ResponseEntity<Void> usePoint(@RequestHeader("X-Saga-Id") UUID sagaId,
                                              @RequestBody PointUsageRequest request) {
        orderPointService.usePoint(sagaId, request);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/members/points/increase")
    public ResponseEntity<Void> increasePoint(@RequestHeader("X-Saga-Id") UUID sagaId,
                                              @RequestBody PointUsageRequest request) {
        orderPointService.refundPoint(sagaId, request);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/members/points/rollback")
    public ResponseEntity<Void> rollbackPoint(@RequestHeader("X-Saga-Id") UUID sagaId,
                                              @RequestBody PointUsageRequest request) {
        orderPointService.rollbackPoint(sagaId, request);

        return ResponseEntity.noContent().build();
    }

    // 포인트 적립은 '사가'는 아니지만 이미 만들어진 멱등성 보장 메커니즘을 재활용
    @PatchMapping("/members/points/accumulate")
    public ResponseEntity<Void> accumulatePoint(@RequestHeader("X-Saga-Id") UUID sagaId,
                                                @RequestBody PointAccumulateRequest request) {
        orderPointService.accumulatePoint(sagaId, request);

        return ResponseEntity.noContent().build();
    }
}
