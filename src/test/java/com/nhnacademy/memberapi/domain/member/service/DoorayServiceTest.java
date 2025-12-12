package com.nhnacademy.memberapi.domain.member.service;

import com.nhnacademy.memberapi.global.error.exception.DoorayMessageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DoorayServiceTest {

    @InjectMocks
    private DoorayService doorayService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RestTemplate restTemplate;

    private final String email = "test@nhn.com";
    private final String hookUrl = "https://hook.dooray.com/services/test";
    private final String redisPrefix = "DORMANT_AUTH:";

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    @Test
    @DisplayName("인증번호 생성 및 전송 성공")
    void sendDormantVerificationCode_Success() {
        doorayService.sendDormantVerificationCode(email, hookUrl);

        ArgumentCaptor<String> redisKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> redisValueCaptor = ArgumentCaptor.forClass(String.class);

        verify(valueOperations).set(
                redisKeyCaptor.capture(),
                redisValueCaptor.capture(),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );

        assertThat(redisKeyCaptor.getValue()).isEqualTo(redisPrefix + email);
        String generatedCode = redisValueCaptor.getValue();
        assertThat(generatedCode).hasSize(6);

        ArgumentCaptor<HttpEntity<Map<String, String>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).postForEntity(
                eq(hookUrl),
                entityCaptor.capture(),
                eq(String.class)
        );

        Map<String, String> body = entityCaptor.getValue().getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("botName")).isEqualTo("휴면인증");
        assertThat(body.get("text")).contains(generatedCode); // 메시지에 코드가 포함되어야 함
    }

    @Test
    @DisplayName("인증번호 전송 실패 - 외부 API 오류 발생")
    void sendDormantVerificationCode_Fail_ApiError() {
        given(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .willThrow(new RuntimeException("Dooray API Error"));

        assertThatThrownBy(() -> doorayService.sendDormantVerificationCode(email, hookUrl))
                .isInstanceOf(DoorayMessageException.class)
                .hasMessage("인증번호 전송에 실패했습니다.");
    }

    @Test
    @DisplayName("인증번호 검증 성공")
    void verifyDormantCode_Success() {
        String inputCode = "123456";
        given(valueOperations.get(redisPrefix + email)).willReturn(inputCode);

        boolean result = doorayService.verifyDormantCode(email, inputCode);

        assertThat(result).isTrue();
        verify(redisTemplate).delete(redisPrefix + email);
    }

    @Test
    @DisplayName("인증번호 검증 실패 - 코드가 일치하지 않음")
    void verifyDormantCode_Fail_WrongCode() {
        String realCode = "123456";
        String wrongInput = "000000";
        given(valueOperations.get(redisPrefix + email)).willReturn(realCode);

        boolean result = doorayService.verifyDormantCode(email, wrongInput);

        assertThat(result).isFalse();
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("인증번호 검증 실패 - 만료되었거나 키가 없음")
    void verifyDormantCode_Fail_Expired() {
        String inputCode = "123456";

        given(valueOperations.get(redisPrefix + email)).willReturn(null);

        boolean result = doorayService.verifyDormantCode(email, inputCode);

        assertThat(result).isFalse();
        verify(redisTemplate, never()).delete(anyString());
    }
}