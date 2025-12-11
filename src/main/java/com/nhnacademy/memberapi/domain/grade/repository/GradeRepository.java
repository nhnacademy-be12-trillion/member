package com.nhnacademy.memberapi.domain.grade.repository;

import com.nhnacademy.memberapi.domain.grade.entity.Grade;
import com.nhnacademy.memberapi.domain.grade.entity.GradeName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    Optional<Grade> findByGradeName(GradeName gradeName);
}
