package com.nhnacademy.memberapi.domain.grade.repository;

import com.nhnacademy.memberapi.domain.grade.entity.Grade;
import com.nhnacademy.memberapi.domain.grade.entity.GradeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.config.import=optional:configserver:"
})
class GradeRepositoryTest {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Grade createAndPersistGrade(GradeName name, BigDecimal ratio, Integer condition) {
        Grade grade = Grade.builder()
                .gradeName(name)
                .gradePointRatio(ratio)
                .gradeCondition(condition)
                .build();
        return entityManager.persist(grade);
    }

    @Test
    @DisplayName("GradeName으로 등급 조회 성공")
    void findByGradeName_Success() {
        GradeName testGradeName = GradeName.ROYAL;
        createAndPersistGrade(testGradeName, BigDecimal.valueOf(0.05), 5000);
        createAndPersistGrade(GradeName.COMMON, BigDecimal.valueOf(0.01), 1000);

        Optional<Grade> actualGrade = gradeRepository.findByGradeName(testGradeName);

        assertThat(actualGrade).isPresent();
        assertThat(actualGrade.get().getGradeName()).isEqualTo(testGradeName);
        assertThat(actualGrade.get().getGradePointRatio()).isEqualTo(BigDecimal.valueOf(0.05));
    }

    @Test
    @DisplayName("GradeName으로 등급 조회 실패_일치하는 등급 없음")
    void findByGradeName_Fail_NotFound() {
        createAndPersistGrade(GradeName.COMMON, BigDecimal.valueOf(0.01), 1000);
        GradeName notFoundGradeName = GradeName.GOLD;

        Optional<Grade> actualGrade = gradeRepository.findByGradeName(notFoundGradeName);

        assertThat(actualGrade).isEmpty();
    }
}