package com.nhnacademy.memberapi.controller;

import com.nhnacademy.memberapi.dto.CustomUserDetails;
import com.nhnacademy.memberapi.dto.request.BookPointRequest;
import com.nhnacademy.memberapi.dto.request.PointRefundRequest;
import com.nhnacademy.memberapi.dto.request.PointUseRequest;
import com.nhnacademy.memberapi.dto.request.ReviewPointRequest;
import com.nhnacademy.memberapi.dto.response.PointHistoryResponse;
import com.nhnacademy.memberapi.service.PointHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/points")
@RequiredArgsConstructor
public class PointController {

    private final PointHistoryService pointHistoryService;

    // 포인트 사용 내역 조회 (마이페이지)
    @GetMapping("/histories")
    public ResponseEntity<List<PointHistoryResponse>> getPointHistories(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
            ) {
        return ResponseEntity.ok(pointHistoryService.getHistories(customUserDetails.getMemberId()));
    }

    // 회원 가입 적립
    @PostMapping("/signup")
    public ResponseEntity<Void> awardSignupPoints(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        pointHistoryService.awardSignupPoints(customUserDetails.getMemberId());
        return ResponseEntity.ok().build();
    }

    // 도서 구매 포인트 적립 (주문 -> 회원)
    @PostMapping("/purchase")
    public ResponseEntity<Void> awardPurchasePoints(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody BookPointRequest request) {

        pointHistoryService.awardPurchasePoints(customUserDetails.getMemberId(), request.orderId(), request.amount());
        return ResponseEntity.ok().build();
    }

    // 리뷰 작성 포인트 적립 (리뷰 -> 회원)
    @PostMapping("/review")
    public ResponseEntity<Void> awardReviewPoints(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody ReviewPointRequest request) {

        pointHistoryService.awardReviewPoints(customUserDetails.getMemberId(), request);
        return ResponseEntity.ok().build();
    }

    // 도서 환불 포인트 반환
    @PostMapping("/refund")
    public ResponseEntity<Void> refundPoints(
            @Valid @RequestBody PointRefundRequest request) {

        pointHistoryService.refundPurchasePoints(request.memberId(), request.orderId());
        return ResponseEntity.ok().build();
    }

    // 포인트 사용 (주문 -> 회원)
    @PostMapping("/use")
    public ResponseEntity<Void> usePoints(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody PointUseRequest request) {

        pointHistoryService.usePoints(customUserDetails.getMemberId(), request.orderId(), request.amount());
        return ResponseEntity.ok().build();
    }
}
