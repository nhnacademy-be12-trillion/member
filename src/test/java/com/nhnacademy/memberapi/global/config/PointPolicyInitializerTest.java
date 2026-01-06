package com.nhnacademy.memberapi.global.config;

import com.nhnacademy.memberapi.domain.point.entity.PointPolicy;
import com.nhnacademy.memberapi.domain.point.entity.PointPolicyCode;
import com.nhnacademy.memberapi.domain.point.repository.PointPolicyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointPolicyInitializerTest {

    @InjectMocks
    private PointPolicyInitializer pointPolicyInitializer;

    @Mock
    private PointPolicyRepository pointPolicyRepository;

    @Test
    @DisplayName("정책 초기화 - DB가 비어있으면 모든 정책 저장")
    void run_SaveAll() throws Exception {
        given(pointPolicyRepository.findByPointPolicyCode(any(PointPolicyCode.class)))
                .willReturn(Optional.empty());

        pointPolicyInitializer.run();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PointPolicy>> captor = ArgumentCaptor.forClass(List.class);

        verify(pointPolicyRepository, times(1)).saveAll(captor.capture());

        List<PointPolicy> savedPolicies = captor.getValue();
        assertThat(savedPolicies).hasSize(PointPolicyCode.values().length);
    }

    @Test
    @DisplayName("정책 초기화 - DB에 데이터가 이미 있으면 저장 안함")
    void run_NoSave() throws Exception {
        given(pointPolicyRepository.findByPointPolicyCode(any(PointPolicyCode.class)))
                .willReturn(Optional.of(PointPolicy.builder().build()));

        pointPolicyInitializer.run();

        verify(pointPolicyRepository, never()).saveAll(any());
    }
}