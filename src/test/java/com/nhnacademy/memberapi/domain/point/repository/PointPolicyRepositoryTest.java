package com.nhnacademy.memberapi.domain.point.repository;

import com.nhnacademy.memberapi.domain.point.entity.PointPolicy;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicyCode;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.config.import=optional:configserver:"
})
class PointPolicyRepositoryTest {

    @Autowired
    private PointPolicyRepository pointPolicyRepository;

    @Test
    @DisplayName("정책 코드로 포인트 정책 조회")
    void findByPointPolicyCode() {
        PointPolicy policy = PointPolicy.builder()
                .pointPolicyCode(PointPolicyCode.SIGNUP)
                .pointPolicyType(PointPolicyType.AMOUNT)
                .pointPolicyName("회원가입 축하금")
                .pointPolicyFixedAmount(5000)
                .lastModifiedAt(LocalDateTime.now())
                .build();

        pointPolicyRepository.save(policy);

        Optional<PointPolicy> result = pointPolicyRepository.findByPointPolicyCode(PointPolicyCode.SIGNUP);

        assertThat(result).isPresent();
        assertThat(result.get().getPointPolicyName()).isEqualTo("회원가입 축하금");
        assertThat(result.get().getPointPolicyFixedAmount()).isEqualTo(5000);
    }

    @Test
    @DisplayName("정책 코드로 포인트 정책 조회 - 실패 (존재하지 않음)")
    void findByPointPolicyCode_NotFound() {

        Optional<PointPolicy> result = pointPolicyRepository.findByPointPolicyCode(PointPolicyCode.REVIEW_BASE);

        assertThat(result).isEmpty();
    }
}