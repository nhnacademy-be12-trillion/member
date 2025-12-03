package com.nhnacademy.memberapi.controller;

import com.nhnacademy.memberapi.dto.request.GradeRequest;
import com.nhnacademy.memberapi.dto.response.GradeResponse;
import com.nhnacademy.memberapi.service.GradeService;
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
@RequestMapping("/api/members/grades")
public class GradeController {

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
