package com.nhnacademy.memberapi.domain.point.controller.docs;

import com.nhnacademy.memberapi.domain.point.dto.PointPolicyResponse;
import com.nhnacademy.memberapi.domain.point.dto.PointPolicyUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Point Policy", description = "포인트 정책 관리 API (관리자용)")
public interface PointPolicyControllerDocs {

    @Operation(summary = "전체 정책 조회", description = "등록된 모든 포인트 정책을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "정책 목록 조회 성공")
    ResponseEntity<List<PointPolicyResponse>> getPolicies();

    @Operation(summary = "단건 정책 조회", description = "특정 포인트 정책의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "정책 조회 성공")
    ResponseEntity<PointPolicyResponse> getPolicy(
            @Parameter(description = "정책 ID", required = true) @PathVariable Long policyId
    );

    @Operation(summary = "정책 수정", description = "포인트 정책의 값이나 타입을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "정책 수정 성공")
    ResponseEntity<PointPolicyResponse> updatePolicyValue(
            @Parameter(description = "정책 ID", required = true) @PathVariable Long policyId,
            @RequestBody PointPolicyUpdateRequest request
    );
}