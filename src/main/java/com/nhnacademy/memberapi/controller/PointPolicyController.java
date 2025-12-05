package com.nhnacademy.memberapi.controller;

import com.nhnacademy.memberapi.dto.request.PointPolicyUpdateRequest;
import com.nhnacademy.memberapi.dto.response.PointPolicyResponse;
import com.nhnacademy.memberapi.service.PointPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/admin/points/policies")
@RequiredArgsConstructor
public class PointPolicyController {

    private final PointPolicyService pointPolicyService;

    // 전체 조회
    @GetMapping
    public ResponseEntity<List<PointPolicyResponse>> getPolicies() {
        return ResponseEntity.ok(pointPolicyService.getPolicies());
    }

    // 단건 조회
    @GetMapping("/{policyId}")
    public ResponseEntity<PointPolicyResponse> getPolicy(@PathVariable Long policyId) {
        return ResponseEntity.ok(pointPolicyService.getPolicy(policyId));
    }

    // 값/타입 수정
    @PutMapping("/{policyId}")
    public ResponseEntity<PointPolicyResponse> updatePolicyValue(
            @PathVariable Long policyId,
            @RequestBody PointPolicyUpdateRequest request) {
        return ResponseEntity.ok(pointPolicyService.updatePolicy(policyId, request));
    }
}