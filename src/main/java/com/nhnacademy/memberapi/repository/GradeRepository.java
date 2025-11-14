package com.nhnacademy.memberapi.repository;

import com.nhnacademy.memberapi.entity.Grade;
import com.nhnacademy.memberapi.entity.GradeName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    Optional<Grade> findByGradeName(GradeName gradeName);
}
