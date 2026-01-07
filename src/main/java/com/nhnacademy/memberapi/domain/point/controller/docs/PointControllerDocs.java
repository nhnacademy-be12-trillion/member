package com.nhnacademy.memberapi.domain.point.controller.docs;

import com.nhnacademy.memberapi.domain.point.dto.PointHistoryResponse;
import com.nhnacademy.memberapi.domain.point.dto.PointUseRequest;
import com.nhnacademy.memberapi.domain.point.dto.ReviewPointRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Tag(name = "Point", description = "포인트 관리 API")
public interface PointControllerDocs {

    @Operation(summary = "포인트 사용 내역 조회", description = "회원의 포인트 적립/사용 내역을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "내역 조회 성공")
    ResponseEntity<List<PointHistoryResponse>> getPointHistories(
            @Parameter(description = "회원 ID", required = true) @RequestHeader("X-Member-Id") Long memberId
    );

    @Operation(summary = "회원가입 포인트 적립", description = "회원가입 축하 포인트를 적립합니다.")
    @ApiResponse(responseCode = "200", description = "적립 성공")
    ResponseEntity<Void> awardSignupPoints(
            @Parameter(description = "회원 ID", required = true) @RequestHeader("X-Member-Id") Long memberId
    );

    @Operation(summary = "리뷰 작성 포인트 적립", description = "상품 리뷰 작성 시 포인트를 적립합니다.")
    @ApiResponse(responseCode = "200", description = "적립 성공")
    ResponseEntity<Void> awardReviewPoints(
            @Parameter(description = "회원 ID", required = true) @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody ReviewPointRequest request
    );

    @Operation(summary = "포인트 사용", description = "주문 결제 시 포인트를 차감합니다.")
    @ApiResponse(responseCode = "200", description = "사용 성공")
    ResponseEntity<Void> usePoints(
            @Parameter(description = "회원 ID", required = true) @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody PointUseRequest request
    );
}