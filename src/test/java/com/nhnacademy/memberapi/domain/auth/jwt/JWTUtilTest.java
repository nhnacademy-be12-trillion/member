package com.nhnacademy.memberapi.domain.auth.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JWTUtilTest {

    private final String testSecret = "testKeyTestKeyTestKeyTestKeyTestKeyTestKeyTestKey123456";

    private final JWTUtil jwtUtil = new JWTUtil(testSecret);

    @Test
    @DisplayName("토큰 생성 및 정보 추출 테스트")
    void createAndParseToken() {
        Long memberId = 1L;
        String role = "ROLE_USER";
        String category = "access";
        Long expiredMs = 60000L; // 1분

        String token = jwtUtil.createJwt(memberId, category, role, expiredMs);

        assertThat(token).isNotNull();
        assertThat(jwtUtil.getMemberId(token)).isEqualTo(memberId);
        assertThat(jwtUtil.getRole(token)).isEqualTo(role);
        assertThat(jwtUtil.getCategory(token)).isEqualTo(category);
        assertThat(jwtUtil.isExpired(token)).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 확인 테스트")
    void expiredTokenTest() throws InterruptedException {
        // 만료시간 (1ms)
        String token = jwtUtil.createJwt(1L, "access", "USER", 1L);

        // 시간 지나게 함
        Thread.sleep(10);

        assertThat(jwtUtil.isExpired(token)).isTrue();
    }
}