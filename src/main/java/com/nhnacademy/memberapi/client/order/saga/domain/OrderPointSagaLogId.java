package com.nhnacademy.memberapi.client.order.saga.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;

@Embeddable
public record OrderPointSagaLogId(
    UUID sagaId,

    @Enumerated(EnumType.STRING)
    OrderSagaType sagaType
) {}
