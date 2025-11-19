package com.nhnacademy.memberapi.entity;

import jakarta.persistence.*;
import lombok.*;

// 등급 정책
@Entity
@Table(name = "Grade")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "grade_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private GradeName gradeName;

    @Column(name = "grade_point_ratio", nullable = false)
    private Integer gradePointRatio;

    @Column(name = "grade_condition", nullable = false)
    private Integer gradeCondition;
}
