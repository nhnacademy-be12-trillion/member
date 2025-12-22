package com.nhnacademy.memberapi.client.order.saga.repository;

import com.nhnacademy.memberapi.client.order.saga.domain.OrderPointSagaLog;
import com.nhnacademy.memberapi.client.order.saga.domain.OrderPointSagaLogId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPointSagaRepository extends JpaRepository<OrderPointSagaLog, OrderPointSagaLogId> {
}
