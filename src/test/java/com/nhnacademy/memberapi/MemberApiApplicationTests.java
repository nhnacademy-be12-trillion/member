package com.nhnacademy.memberapi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled
@SpringBootTest(properties = {
        "eureka.client.enabled=false",        // 유레카 클라이언트 비활성화
        "spring.cloud.discovery.enabled=false",  // Spring Cloud 디스커버리 기능 비활성화
        "spring.cloud.config.enabled=false",
        "spring.jwt.secret=testKeyasfklsadjdgljdsalghaslghaasdflasdfsjg"
})
class MemberApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
