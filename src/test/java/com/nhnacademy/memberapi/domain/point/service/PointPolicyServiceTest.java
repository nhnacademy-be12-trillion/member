package com.nhnacademy.memberapi.domain.point.service;

import com.nhnacademy.memberapi.domain.point.dto.PointPolicyResponse;
import com.nhnacademy.memberapi.domain.point.dto.PointPolicyUpdateRequest;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicy;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicyCode;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicyType;
import com.nhnacademy.memberapi.domain.point.repository.PointPolicyRepository;
import com.nhnacademy.memberapi.global.error.exception.InvalidPointPolicyTypeException;
import com.nhnacademy.memberapi.global.error.exception.PointPolicyNotFoundException;
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

@ExtendWith(MockitoExtension.class)
class PointPolicyServiceTest {

    @InjectMocks
    private PointPolicyService pointPolicyService;

    @Mock
    private PointPolicyRepository pointPolicyRepository;

    @Test
    @DisplayName("정책 코드로 조회")
    void getPolicyByCode() {
        PointPolicyCode code = PointPolicyCode.SIGNUP;
        PointPolicy policy = PointPolicy.builder().pointPolicyCode(code).build();

        given(pointPolicyRepository.findByPointPolicyCode(code)).willReturn(Optional.of(policy));

        PointPolicy result = pointPolicyService.getPolicyByCode(code);

        assertThat(result).isEqualTo(policy);
    }

    @Test
    @DisplayName("정책 코드로 조회 실패")
    void getPolicyByCode_NotFound() {
        PointPolicyCode code = PointPolicyCode.SIGNUP;
        given(pointPolicyRepository.findByPointPolicyCode(code)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pointPolicyService.getPolicyByCode(code))
                .isInstanceOf(PointPolicyNotFoundException.class);
    }

    @Test
    @DisplayName("전체 정책 조회")
    void getPolicies() {
        PointPolicy policy = PointPolicy.builder().pointPolicyId(1L).build();
        given(pointPolicyRepository.findAll()).willReturn(List.of(policy));

        List<PointPolicyResponse> results = pointPolicyService.getPolicies();

        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("단건 정책 조회")
    void getPolicy() {
        Long policyId = 1L;
        PointPolicy policy = PointPolicy.builder().pointPolicyId(policyId).build();
        given(pointPolicyRepository.findById(policyId)).willReturn(Optional.of(policy));

        PointPolicyResponse result = pointPolicyService.getPolicy(policyId);

        assertThat(result.pointPolicyId()).isEqualTo(policyId);
    }

    @Test
    @DisplayName("정책 수정 - RATE 타입")
    void updatePolicy_Rate() {
        Long policyId = 1L;
        PointPolicy policy = PointPolicy.builder()
                .pointPolicyId(policyId)
                .pointPolicyCode(PointPolicyCode.PURCHASE)
                .pointPolicyType(PointPolicyType.RATE)
                .pointPolicyName("기존 정책")
                .build();

        PointPolicyUpdateRequest request = new PointPolicyUpdateRequest(
                "수정된 정책", PointPolicyType.RATE, new BigDecimal("0.05"), null
        );

        given(pointPolicyRepository.findById(policyId)).willReturn(Optional.of(policy));

        PointPolicyResponse response = pointPolicyService.updatePolicy(policyId, request);

        assertThat(response.pointPolicyRate()).isEqualTo(new BigDecimal("0.05"));
        assertThat(response.pointPolicyName()).isEqualTo("수정된 정책");
        assertThat(policy.getPointPolicyFixedAmount()).isNull();
    }

    @Test
    @DisplayName("정책 수정 - AMOUNT 타입")
    void updatePolicy_Amount() {
        Long policyId = 1L;
        PointPolicy policy = PointPolicy.builder()
                .pointPolicyId(policyId)
                .pointPolicyCode(PointPolicyCode.SIGNUP)
                .pointPolicyType(PointPolicyType.AMOUNT)
                .pointPolicyName("기존 정책")
                .build();

        PointPolicyUpdateRequest request = new PointPolicyUpdateRequest(
                "수정된 정책", PointPolicyType.AMOUNT, null, 10000
        );

        given(pointPolicyRepository.findById(policyId)).willReturn(Optional.of(policy));

        PointPolicyResponse response = pointPolicyService.updatePolicy(policyId, request);

        assertThat(response.pointPolicyFixedAmount()).isEqualTo(10000);
        assertThat(response.pointPolicyName()).isEqualTo("수정된 정책");
        assertThat(policy.getPointPolicyRate()).isNull();
    }

    @Test
    @DisplayName("정책 수정 실패 - RATE 타입인데 rate 값 누락")
    void updatePolicy_Rate_MissingValue_Fail() {
        Long policyId = 1L;
        PointPolicy policy = PointPolicy.builder().pointPolicyId(policyId).build();

        PointPolicyUpdateRequest request = new PointPolicyUpdateRequest(
                "실패 정책", PointPolicyType.RATE, null, 1000
        );

        given(pointPolicyRepository.findById(policyId)).willReturn(Optional.of(policy));

        assertThatThrownBy(() -> pointPolicyService.updatePolicy(policyId, request))
                .isInstanceOf(InvalidPointPolicyTypeException.class)
                .hasMessageContaining("RATE 타입은 적립률(rate)이 필수입니다.");
    }

    @Test
    @DisplayName("정책 수정 실패 - AMOUNT 타입인데 fixedAmount 값 누락")
    void updatePolicy_Amount_MissingValue_Fail() {
        Long policyId = 1L;
        PointPolicy policy = PointPolicy.builder().pointPolicyId(policyId).build();

        PointPolicyUpdateRequest request = new PointPolicyUpdateRequest(
                "실패 정책", PointPolicyType.AMOUNT, new BigDecimal("0.1"), null
        );

        given(pointPolicyRepository.findById(policyId)).willReturn(Optional.of(policy));

        assertThatThrownBy(() -> pointPolicyService.updatePolicy(policyId, request))
                .isInstanceOf(InvalidPointPolicyTypeException.class)
                .hasMessageContaining("AMOUNT 타입은 고정 금액(fixedAmount)이 필수입니다.");
    }

    @Test
    @DisplayName("정책 수정 실패 - 정책을 찾을 수 없음")
    void updatePolicy_NotFound() {
        given(pointPolicyRepository.findById(999L)).willReturn(Optional.empty());

        PointPolicyUpdateRequest request = new PointPolicyUpdateRequest("test", PointPolicyType.RATE, BigDecimal.ONE, null);

        assertThatThrownBy(() -> pointPolicyService.updatePolicy(999L, request))
                .isInstanceOf(PointPolicyNotFoundException.class);
    }
}