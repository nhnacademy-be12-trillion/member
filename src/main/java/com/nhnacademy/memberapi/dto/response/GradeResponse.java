package com.nhnacademy.memberapi.dto.response;

import com.nhnacademy.memberapi.entity.Grade;
import com.nhnacademy.memberapi.entity.GradeName;

public record GradeResponse(
    Long gradeId,
    GradeName gradeName,
    Integer gradePointRatio,
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