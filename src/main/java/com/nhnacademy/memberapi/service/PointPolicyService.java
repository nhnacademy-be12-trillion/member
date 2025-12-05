package com.nhnacademy.memberapi.service;

import com.nhnacademy.memberapi.dto.request.PointPolicyUpdateRequest;
import com.nhnacademy.memberapi.dto.response.PointPolicyResponse;
import com.nhnacademy.memberapi.entity.PointPolicy;
import com.nhnacademy.memberapi.entity.PointPolicyCode;
import com.nhnacademy.memberapi.entity.PointPolicyType;
import com.nhnacademy.memberapi.exception.InvalidPointPolicyTypeException;
import com.nhnacademy.memberapi.exception.PointPolicyNotFoundException;
import com.nhnacademy.memberapi.repository.PointPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PointPolicyService {

    private final PointPolicyRepository pointPolicyRepository;

    // 정책 코드로 가져오기
    @Transactional(readOnly = true)
    public PointPolicy getPolicyByCode(PointPolicyCode code) {
        return pointPolicyRepository.findByPointPolicyCode(code)
                .orElseThrow(() -> new PointPolicyNotFoundException("Policy not found for code: " + code));
    }

    // 전체 정책 조회 (관리자)
    @Transactional(readOnly = true)
    public List<PointPolicyResponse> getPolicies() {
        return pointPolicyRepository.findAll().stream()
                .map(PointPolicyResponse::from)
                .collect(Collectors.toList());
    }

    // 단건 정책 조회 (관리자)
    @Transactional(readOnly = true)
    public PointPolicyResponse getPolicy(Long policyId) {
        PointPolicy policy = pointPolicyRepository.findById(policyId)
                .orElseThrow(() -> new PointPolicyNotFoundException("Policy not found: " + policyId));
        return PointPolicyResponse.from(policy);
    }

    // 포인트 정책 수정 (관리자)
    public PointPolicyResponse updatePolicy(Long policyId, PointPolicyUpdateRequest request) {
        PointPolicy policy = pointPolicyRepository.findById(policyId)
                .orElseThrow(() -> new PointPolicyNotFoundException("Policy not found: " + policyId));

        // 타입에 맞는 값이 들어왔는지 유효성 검사
        if (request.pointPolicyType() == PointPolicyType.RATE) {
            if (request.pointPolicyRate() == null) {
                throw new InvalidPointPolicyTypeException("RATE 타입은 적립률(rate)이 필수입니다.");
            }
        } else if (request.pointPolicyType() == PointPolicyType.AMOUNT) {
            if (request.pointPolicyFixedAmount() == null) {
                throw new InvalidPointPolicyTypeException("AMOUNT 타입은 고정 금액(fixedAmount)이 필수입니다.");
            }
        }

        // 엔티티가 스스로 타입에 따라 나머지 필드를 null로 만듦
        policy.updatePolicy(
                request.pointPolicyName(),
                request.pointPolicyType(),
                request.pointPolicyRate(),
                request.pointPolicyFixedAmount()
        );

        return PointPolicyResponse.from(policy);
    }
}