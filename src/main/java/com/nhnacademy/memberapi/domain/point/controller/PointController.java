package com.nhnacademy.memberapi.domain.point.controller;

import com.nhnacademy.memberapi.domain.point.dto.PointHistoryResponse;
import com.nhnacademy.memberapi.domain.point.dto.PointUseRequest;
import com.nhnacademy.memberapi.domain.point.dto.ReviewPointRequest;
import com.nhnacademy.memberapi.domain.point.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members/points")
@RequiredArgsConstructor
public class PointController {

    private final PointHistoryService pointHistoryService;

    // 포인트 사용 내역 조회 (마이페이지)
    @GetMapping("/histories")
    public ResponseEntity<List<PointHistoryResponse>> getPointHistories(
            @RequestHeader("X-Member-Id") Long memberId
            ) {
        return ResponseEntity.ok(pointHistoryService.getHistories(memberId));
    }

    // 회원 가입 적립
    @PostMapping("/signup")
    public ResponseEntity<Void> awardSignupPoints(@RequestHeader("X-Member-Id") Long memberId){
        pointHistoryService.awardSignupPoints(memberId);
        return ResponseEntity.ok().build();
    }

    // 리뷰 작성 포인트 적립 (리뷰 -> 회원)
    @PostMapping("/review")
    public ResponseEntity<Void> awardReviewPoints(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody ReviewPointRequest request) {

        pointHistoryService.awardReviewPoints(memberId, request);
        return ResponseEntity.ok().build();
    }

    // 포인트 사용 (주문 -> 회원)
    @PostMapping("/use")
    public ResponseEntity<Void> usePoints(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody PointUseRequest request) {

        pointHistoryService.usePoints(memberId, request.orderId(), request.amount());
        return ResponseEntity.ok().build();
    }
}
