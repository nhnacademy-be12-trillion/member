package com.nhnacademy.memberapi.global.config;

import com.nhnacademy.memberapi.domain.grade.entity.Grade;
import com.nhnacademy.memberapi.domain.grade.entity.GradeName;
import com.nhnacademy.memberapi.domain.grade.repository.GradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeInitializerTest {

    @InjectMocks
    private GradeInitializer gradeInitializer;

    @Mock
    private GradeRepository gradeRepository;

    @Test
    @DisplayName("초기화 실행 - 모든 등급이 DB에 없을 때 전부 저장")
    void run_SaveAll_WhenDbIsEmpty() throws Exception {
        // 어떤 등급 이름으로 조회하든 비어있다고(Optional.empty) 반환 설정
        given(gradeRepository.findByGradeName(any(GradeName.class))).willReturn(Optional.empty());

        gradeInitializer.run();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Grade>> captor = ArgumentCaptor.forClass(List.class);

        // saveAll이 한 번 호출되었는지 확인하고, 인자를 캡처
        verify(gradeRepository, times(1)).saveAll(captor.capture());

        List<Grade> savedGrades = captor.getValue();
        // 저장된 리스트의 크기가 GradeName Enum의 전체 개수와 같은지 검증
        assertThat(savedGrades).hasSize(GradeName.values().length);
    }

    @Test
    @DisplayName("초기화 실행 - 이미 모든 등급이 DB에 있을 때 저장하지 않음")
    void run_NoSave_WhenDbIsFull() throws Exception {
        // 어떤 등급 이름으로 조회하든 이미 존재한다고(Grade 객체 반환) 설정
        given(gradeRepository.findByGradeName(any(GradeName.class)))
                .willReturn(Optional.of(Grade.builder().build()));

        gradeInitializer.run();

        // saveAll 메서드가 절대 호출되지 않았는지 검증
        verify(gradeRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("초기화 실행 - 일부 등급만 없을 때 없는 것만 저장")
    void run_SavePartial_WhenSomeMissing() throws Exception {
        // COMMON 등급은 이미 있고, 나머지는 없다고 가정
        given(gradeRepository.findByGradeName(GradeName.COMMON))
                .willReturn(Optional.of(Grade.builder().gradeName(GradeName.COMMON).build()));

        // COMMON이 아닌 다른 등급들은 비어있음
        // (Mockito는 명시하지 않은 호출에 대해 기본적으로 null/empty를 반환하지만 명시적으로 작성)
        given(gradeRepository.findByGradeName(argThat(name -> name != GradeName.COMMON)))
                .willReturn(Optional.empty());

        gradeInitializer.run();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Grade>> captor = ArgumentCaptor.forClass(List.class);
        verify(gradeRepository).saveAll(captor.capture());

        List<Grade> savedGrades = captor.getValue();
        // 전체 개수 - 1개만큼 저장되어야 함
        assertThat(savedGrades).hasSize(GradeName.values().length - 1);
        // 저장된 리스트에 COMMON은 없어야 함
        boolean hasCommon = savedGrades.stream()
                .anyMatch(g -> g.getGradeName() == GradeName.COMMON);
        assertThat(hasCommon).isFalse();
    }
}