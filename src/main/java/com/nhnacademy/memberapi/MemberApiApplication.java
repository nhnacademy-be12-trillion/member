package com.nhnacademy.memberapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableRedisRepositories
public class MemberApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemberApiApplication.class, args);
    }

}
