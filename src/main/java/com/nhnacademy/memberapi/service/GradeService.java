package com.nhnacademy.memberapi.service;

import com.nhnacademy.memberapi.dto.request.GradeRequest;
import com.nhnacademy.memberapi.dto.response.GradeResponse;
import com.nhnacademy.memberapi.entity.Grade;
import com.nhnacademy.memberapi.exception.GradeNotFoundException;
import com.nhnacademy.memberapi.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/*
 등급에 대한 데이터는 DB에 초기 데이터로 추가
 foreign key 제약 조건 위반을 방지 하기 위해 관리자 페이지에서는 등급 조회와 수정만 지원
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GradeService {

    private final GradeRepository gradeRepository;

    // 전체 조회
    @Transactional(readOnly = true)
    public List<GradeResponse> getGrades() {
        return gradeRepository.findAll().stream()
                .map(GradeResponse::from)
                .collect(Collectors.toList());
    }

    // 단건 조회
    @Transactional(readOnly = true)
    public GradeResponse getGrade(Long gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new GradeNotFoundException("Grade not found: " + gradeId));
        return GradeResponse.from(grade);
    }

    // 등급 정책 수정
    public GradeResponse updateGrade(Long gradeId, GradeRequest request) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new GradeNotFoundException("Grade not found: " + gradeId));

        grade.updatePolicy(request.gradePointRatio(), request.gradeCondition());

        return GradeResponse.from(grade);
    }
}