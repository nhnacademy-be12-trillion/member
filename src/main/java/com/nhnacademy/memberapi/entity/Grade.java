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

    // 등급 적립률
    @Column(name = "grade_point_ratio", nullable = false)
    private Integer gradePointRatio;

    // 등급 조건
    @Column(name = "grade_condition", nullable = false)
    private Integer gradeCondition;

    /*
     등급 정책 업데이트
     적립률과 적립 조건에 잘못된 값이 들어오는 경우 검증
     */
    public void updatePolicy(Integer gradePointRatio, Integer gradeCondition) {
        if (gradePointRatio != null && gradePointRatio >= 0) {
            this.gradePointRatio = gradePointRatio;
        }
        if (gradeCondition != null && gradeCondition >= 0) {
            this.gradeCondition = gradeCondition;
        }
    }

}
