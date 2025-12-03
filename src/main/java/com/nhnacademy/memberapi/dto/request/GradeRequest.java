package com.nhnacademy.memberapi.dto.request;

import com.nhnacademy.memberapi.entity.GradeName;

public record GradeRequest(
        GradeName gradeName,
        Integer gradePointRatio,
        Integer gradeCondition
){}