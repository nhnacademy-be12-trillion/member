package com.nhnacademy.memberapi.dto.response;

import com.nhnacademy.memberapi.entity.PointHistory;

import java.time.LocalDateTime;

public record PointHistoryResponse(
        Long pointHistoryId,
        String reason,
        Integer amount, // 변동 금액
        Integer currentBalance, // 잔액
        LocalDateTime transactionAt
) {
    public static PointHistoryResponse from(PointHistory history) {
        return new PointHistoryResponse(
                history.getPointHistoryId(),
                history.getPointHistoryReason(),
                history.getPointHistoryPoint(),
                history.getCurrentTotalPoint(),
                history.getTransactionAt()
        );
    }
}