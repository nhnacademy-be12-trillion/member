package com.nhnacademy.memberapi.domain.point.repository;

import com.nhnacademy.memberapi.domain.point.entity.PointPolicy;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointPolicyRepository extends JpaRepository<PointPolicy, Long> {
    Optional<PointPolicy> findByPointPolicyCode(PointPolicyCode pointPolicyCode);
}