package com.nhnacademy.memberapi.client.order.controller.docs;

import com.nhnacademy.memberapi.client.order.dto.PointAccumulateRequest;
import com.nhnacademy.memberapi.client.order.dto.PointUsageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@Tag(name = "Order Point Saga", description = "주문 연동 포인트 Saga API")
public interface OrderPointControllerDocs {

    @Operation(summary = "포인트 사용 (Saga)", description = "주문 결제 시 포인트를 사용합니다.")
    @ApiResponse(responseCode = "204", description = "포인트 사용 성공")
    ResponseEntity<Void> usePoint(
            @Parameter(description = "Saga ID", required = true) @RequestHeader("X-Saga-Id") UUID sagaId,
            @RequestBody PointUsageRequest request
    );

    @Operation(summary = "포인트 환불 (Saga)", description = "주문 취소/반품 시 사용했던 포인트를 환불(재적립)합니다.")
    @ApiResponse(responseCode = "204", description = "포인트 환불 성공")
    ResponseEntity<Void> increasePoint(
            @Parameter(description = "Saga ID", required = true) @RequestHeader("X-Saga-Id") UUID sagaId,
            @RequestBody PointUsageRequest request
    );

    @Operation(summary = "포인트 롤백 (Saga)", description = "Saga 보상 트랜잭션으로 포인트 사용을 취소합니다.")
    @ApiResponse(responseCode = "204", description = "포인트 롤백 성공")
    ResponseEntity<Void> rollbackPoint(
            @Parameter(description = "Saga ID", required = true) @RequestHeader("X-Saga-Id") UUID sagaId,
            @RequestBody PointUsageRequest request
    );

    @Operation(summary = "포인트 적립", description = "주문 완료 후 포인트를 적립합니다. (Saga ID 활용 멱등성 보장)")
    @ApiResponse(responseCode = "204", description = "포인트 적립 성공")
    ResponseEntity<Void> accumulatePoint(
            @Parameter(description = "Saga ID", required = true) @RequestHeader("X-Saga-Id") UUID sagaId,
            @RequestBody PointAccumulateRequest request
    );
}
