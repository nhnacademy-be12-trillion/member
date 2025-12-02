package com.nhnacademy.memberapi.dto.request;

import com.nhnacademy.memberapi.entity.GradeName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GradeRequestDto {
    private GradeName gradeName;
    private Integer gradePointRatio;
    private Integer gradeCondition;
}