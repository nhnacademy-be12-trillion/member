package com.nhnacademy.memberapi.config;

import com.nhnacademy.memberapi.entity.Grade;
import com.nhnacademy.memberapi.entity.GradeName;
import com.nhnacademy.memberapi.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GradeInitializer implements CommandLineRunner {

    private final GradeRepository gradeRepository;

    /*
     * 애플리케이션 시작 시 DB에 등급 데이터가 없다면 삽입
     * 행 단위로 누락 확인
     */
    @Override
    public void run(String... args) throws Exception {
        List<Grade> missingGrades = new ArrayList<>();

        for (GradeName name : GradeName.values()) {
            // GradeName에 존재하는 등급명이 DB에 없다면
            if (gradeRepository.findByGradeName(name).isEmpty()) {
                // 해당 등급 객체를 생성해서 insert
                Grade missingGrade = createGradeData(name);
                missingGrades.add(missingGrade);
                log.warn("초기 등급 데이터 누락: {} 등급을 목록에 추가", name);
            }
        }

        // 누락된 등급이 있는 경우에만 일괄 저장
        if (!missingGrades.isEmpty()) {
            gradeRepository.saveAll(missingGrades);
            log.info("{}개의 초기 등급 데이터 삽입 완료", missingGrades.size());
        }
    }

    // GradeName에 따라 해당 등급의 초기 조건 데이터를 생성
    private Grade createGradeData(GradeName name) {
        return switch (name) {
            case COMMON -> Grade.builder().gradeName(name).gradePointRatio(BigDecimal.valueOf(0.01)).gradeCondition(0).build();
            case ROYAL -> Grade.builder().gradeName(name).gradePointRatio(BigDecimal.valueOf(0.02)).gradeCondition(100000).build();
            case GOLD -> Grade.builder().gradeName(name).gradePointRatio(BigDecimal.valueOf(0.02)).gradeCondition(200000).build();
            case PLATINUM -> Grade.builder().gradeName(name).gradePointRatio(BigDecimal.valueOf(0.03)).gradeCondition(300000).build();
        };
    }
}