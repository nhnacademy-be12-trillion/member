package com.nhnacademy.memberapi.domain.grade.service;

import com.nhnacademy.memberapi.domain.grade.dto.GradeRequest;
import com.nhnacademy.memberapi.domain.grade.dto.GradeResponse;
import com.nhnacademy.memberapi.domain.grade.entity.Grade;
import com.nhnacademy.memberapi.domain.grade.entity.GradeName;
import com.nhnacademy.memberapi.domain.grade.repository.GradeRepository;
import com.nhnacademy.memberapi.global.error.exception.GradeNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @InjectMocks
    private GradeService gradeService;

    @Mock
    private GradeRepository gradeRepository;

    private Grade createGrade(Long id, GradeName name, BigDecimal ratio, Integer condition) {
        return Grade.builder()
                .gradeId(id)
                .gradeName(name)
                .gradePointRatio(ratio)
                .gradeCondition(condition)
                .build();
    }

    @Test
    @DisplayName("전체 등급 조회")
    void getGrades_Success() {
        List<Grade> mockGrades = List.of(
                createGrade(1L, GradeName.COMMON, BigDecimal.valueOf(0.01), 1000),
                createGrade(2L, GradeName.ROYAL, BigDecimal.valueOf(0.05), 5000)
        );
        given(gradeRepository.findAll()).willReturn(mockGrades);

        List<GradeResponse> result = gradeService.getGrades();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).gradeName()).isEqualTo(GradeName.COMMON);
        verify(gradeRepository).findAll();
    }

    @Test
    @DisplayName("특정 등급 단건 조회")
    void getGrade_Success() {
        Long gradeId = 3L;
        Grade mockGrade = createGrade(gradeId, GradeName.GOLD, BigDecimal.valueOf(0.1), 10000);
        given(gradeRepository.findById(gradeId)).willReturn(Optional.of(mockGrade));

        GradeResponse result = gradeService.getGrade(gradeId);

        assertThat(result.gradeId()).isEqualTo(gradeId);
        assertThat(result.gradePointRatio()).isEqualTo(BigDecimal.valueOf(0.1));
    }

    @Test
    @DisplayName("특정 등급 단건 조회 실패(등급 없음)")
    void getGrade_Fail_NotFound() {
        Long gradeId = 99L;
        given(gradeRepository.findById(gradeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.getGrade(gradeId))
                .isInstanceOf(GradeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("등급 정책 수정")
    void updateGrade_Success() {
        Long gradeId = 4L;
        Grade initialGrade = createGrade(gradeId, GradeName.PLATINUM, BigDecimal.valueOf(0.15), 50000);
        GradeRequest request = new GradeRequest(GradeName.GOLD, BigDecimal.valueOf(0.20), 60000);

        given(gradeRepository.findById(gradeId)).willReturn(Optional.of(initialGrade));

        GradeResponse result = gradeService.updateGrade(gradeId, request);

        assertThat(result.gradePointRatio()).isEqualTo(BigDecimal.valueOf(0.20));
        assertThat(result.gradeCondition()).isEqualTo(60000);

        assertThat(initialGrade.getGradePointRatio()).isEqualTo(BigDecimal.valueOf(0.20));
        assertThat(initialGrade.getGradeCondition()).isEqualTo(60000);

    }

    @Test
    @DisplayName("등급 정책 수정 실패(등급 없음)")
    void updateGrade_Fail_NotFound() {
        Long gradeId = 99L;
        GradeRequest request = new GradeRequest(GradeName.GOLD, BigDecimal.valueOf(0.20), 60000);
        given(gradeRepository.findById(gradeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.updateGrade(gradeId, request))
                .isInstanceOf(GradeNotFoundException.class);
    }
}