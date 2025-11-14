package com.nhnacademy.memberapi.repository;

import com.nhnacademy.memberapi.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
