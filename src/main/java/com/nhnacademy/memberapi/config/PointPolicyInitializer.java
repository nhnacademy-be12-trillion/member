package com.nhnacademy.memberapi.config;

import com.nhnacademy.memberapi.entity.PointPolicy;
import com.nhnacademy.memberapi.entity.PointPolicyCode;
import com.nhnacademy.memberapi.repository.PointPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointPolicyInitializer implements CommandLineRunner {

    private final PointPolicyRepository pointPolicyRepository;
    /*
     * 애플리케이션 시작 시 DB에 포인트 정책 데이터가 없다면 삽입
     * 행 단위로 누락 확인
     */
    @Override
    public void run(String... args) throws Exception {
        List<PointPolicy> missingPointPolicies = new ArrayList<>();

        for (PointPolicyCode code : PointPolicyCode.values()) {
            // PointPolicyCode가 DB에 없다면
            if (pointPolicyRepository.findByPointPolicyCode(code).isEmpty()) {
                // 해당 포인트 정책 객체를 생성해서 insert
                missingPointPolicies.add(createPointPolicyData(code));
                log.warn("포인트 정책 데이터 누락: {} 을 목록에 추가", code);
            }
        }
        // 누락된 등급이 있는 경우에만 일괄 저장
        if (!missingPointPolicies.isEmpty()) {
            pointPolicyRepository.saveAll(missingPointPolicies);
            log.info("포인트 정책 데이터 삽입 완료");
        }
    }

    // PointPolicyCode에 따라 초기 데이터 생성
    private PointPolicy createPointPolicyData(PointPolicyCode code) {
        LocalDateTime now = LocalDateTime.now();
        return PointPolicy.builder()
                .pointPolicyCode(code)
                .pointPolicyType(code.getPolicyType())
                .pointPolicyName(code.getDefaultName())
                .pointPolicyRate(code.getDefaultRate())
                .pointPolicyFixedAmount(code.getDefaultAmount())
                .lastModifiedAt(now)
                .build();
    }
}