package com.nhnacademy.memberapi.client.order.saga.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class OrderPointSagaLog {
    @EmbeddedId
    private OrderPointSagaLogId id;

    private LocalDateTime processedAt;

    public OrderPointSagaLog(OrderPointSagaLogId id) {
        this.id = id;
        processedAt = LocalDateTime.now();
    }
}
