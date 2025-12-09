package com.nhnacademy.memberapi.domain.grade.dto;

import com.nhnacademy.memberapi.domain.grade.entity.Grade;
import com.nhnacademy.memberapi.domain.grade.entity.GradeName;
import java.math.BigDecimal;

public record GradeResponse(
    Long gradeId,
    GradeName gradeName,
    BigDecimal gradePointRatio,
    Integer gradeCondition
) {
    public static GradeResponse from(Grade grade) {
        return new GradeResponse(
                grade.getGradeId(),
                grade.getGradeName(),
                grade.getGradePointRatio(),
                grade.getGradeCondition()
        );
    }
}