package com.nhnacademy.memberapi.domain.grade.dto;

import com.nhnacademy.memberapi.domain.grade.entity.GradeName;

import java.math.BigDecimal;

public record GradeRequest(
        GradeName gradeName,
        BigDecimal gradePointRatio,
        Integer gradeCondition
){}