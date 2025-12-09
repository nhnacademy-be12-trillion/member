package com.nhnacademy.memberapi.domain.member.service;

import com.nhnacademy.memberapi.global.error.exception.DoorayMessageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoorayService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RestTemplate restTemplate;

    private static final String REDIS_PREFIX = "DORMANT_AUTH:";
    private static final long EXPIRE_MINUTES = 5;

    // 인증번호 생성 및 두레이 전송
    public void sendDormantVerificationCode(String memberEmail, String doorayHookUrl) {
        // 인증번호 생성 (6자리 난수)
        String verificationCode = String.valueOf((int) (Math.random() * 900000) + 100000);

        // key: REDIS_PREFIX+memberEmail, value: verificationCode
        redisTemplate.opsForValue().set(
                REDIS_PREFIX+memberEmail,
                verificationCode,
                EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        // 두레이 메시지 전송
        try {
            sendDoorayMessage(doorayHookUrl, verificationCode);
        } catch (Exception e) {
            log.error("두레이 메시지 전송 실패: {}", e.getMessage());
            throw new DoorayMessageException("인증번호 전송에 실패했습니다.");
        }
    }

    // 인증번호 검증
    public boolean verifyDormantCode(String memberEmail, String inputCode) {
        String storedCode = redisTemplate.opsForValue().get(REDIS_PREFIX + memberEmail);

        if (storedCode != null && storedCode.equals(inputCode)) {
            redisTemplate.delete(REDIS_PREFIX + memberEmail); // 인증 성공 시 삭제
            return true;
        }
        return false;
    }

    // 실제 두레이 Hook API 호출
    private void sendDoorayMessage(String hookUrl, String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload = new HashMap<>();
        payload.put("botName", "휴면인증");
        payload.put("text", "인증번호: [" + code + "]");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(hookUrl, entity, String.class);
    }
}