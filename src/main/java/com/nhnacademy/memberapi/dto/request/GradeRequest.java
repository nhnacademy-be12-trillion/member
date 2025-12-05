package com.nhnacademy.memberapi.dto.request;

import com.nhnacademy.memberapi.entity.GradeName;

import java.math.BigDecimal;

public record GradeRequest(
        GradeName gradeName,
        BigDecimal gradePointRatio,
        Integer gradeCondition
){}