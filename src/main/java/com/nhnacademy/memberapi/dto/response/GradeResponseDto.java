package com.nhnacademy.memberapi.dto.response;

import com.nhnacademy.memberapi.entity.Grade;
import com.nhnacademy.memberapi.entity.GradeName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GradeResponseDto {
    private Long gradeId;
    private GradeName gradeName;
    private Integer gradePointRatio;
    private Integer gradeCondition;

    public static GradeResponseDto from(Grade grade) {
        return new GradeResponseDto(
                grade.getGradeId(),
                grade.getGradeName(),
                grade.getGradePointRatio(),
                grade.getGradeCondition()
        );
    }
}