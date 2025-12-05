package com.nhnacademy.memberapi.repository;

import com.nhnacademy.memberapi.entity.PointPolicy;
import com.nhnacademy.memberapi.entity.PointPolicyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointPolicyRepository extends JpaRepository<PointPolicy, Long> {
    Optional<PointPolicy> findByPointPolicyCode(PointPolicyCode pointPolicyCode);
}