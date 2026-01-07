package com.nhnacademy.memberapi.domain.grade.controller.docs;

import com.nhnacademy.memberapi.domain.grade.dto.GradeRequest;
import com.nhnacademy.memberapi.domain.grade.dto.GradeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Grade", description = "회원 등급 API")
public interface GradeControllerDocs {

    @Operation(summary = "전체 등급 조회", description = "시스템에 등록된 모든 회원 등급을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "전체 등급 조회 성공")
    ResponseEntity<List<GradeResponse>> getGrades();

    @Operation(summary = "단건 등급 조회", description = "특정 회원 등급의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "등급 조회 성공")
    ResponseEntity<GradeResponse> getGrade(
            @Parameter(description = "등급 ID", required = true) @PathVariable Long gradeId
    );

    @Operation(summary = "등급 정보 수정", description = "회원 등급의 기준(이름, 적립률, 필요 금액 등)을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "등급 수정 성공")
    ResponseEntity<GradeResponse> updateGrade(
            @Parameter(description = "등급 ID", required = true) @PathVariable Long gradeId,
            @RequestBody GradeRequest gradeRequest
    );
}