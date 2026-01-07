package com.nhnacademy.memberapi.domain.grade.controller;

import com.nhnacademy.memberapi.domain.grade.controller.docs.GradeControllerDocs;
import com.nhnacademy.memberapi.domain.grade.dto.GradeRequest;
import com.nhnacademy.memberapi.domain.grade.dto.GradeResponse;
import com.nhnacademy.memberapi.domain.grade.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 특정 멤버의 등급 조회 및 수정은 배치 서비스에 구현
 회원 서비스에서의 등급 관리는 등급 자체에 대한 조회 및 수정(등급명, 적립률, 등급 조건)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/members/grades")
public class GradeController implements GradeControllerDocs {

    private final GradeService gradeService;

    @GetMapping
    public ResponseEntity<List<GradeResponse>> getGrades(){
        return ResponseEntity.ok(gradeService.getGrades());
    }

    @GetMapping("/{gradeId}")
    public ResponseEntity<GradeResponse> getGrade(@PathVariable Long gradeId){
        return ResponseEntity.ok(gradeService.getGrade(gradeId));
    }


    @PutMapping("/{gradeId}")
    public ResponseEntity<GradeResponse> updateGrade(@PathVariable Long gradeId,
                                                     @RequestBody GradeRequest gradeRequest){
        return ResponseEntity.ok(gradeService.updateGrade(gradeId, gradeRequest));
    }
}
