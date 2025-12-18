package com.nhnacademy.memberapi.domain.member.service;

import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import com.nhnacademy.memberapi.global.error.exception.EmailSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberRepository;

    @Async
    public void sendVerificationCode(String email) {
        // 인증번호 6자리 난수 생성
        String code = createRandomCode();

        // 이메일 전송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("회원가입 인증번호");
        message.setText("인증번호: [" + code + "]");
        try{
            javaMailSender.send(message);
        }catch (RuntimeException e){
            log.debug(e.getMessage());
            throw new EmailSendException("이메일 전송 실패");
        }

        String authCode = "AuthCode:" + email;
        // Redis에 저장. 유효기간 3분 (Key: authCode, Value: 6자리 난수)
        redisTemplate.opsForValue().set(authCode, code, 3, TimeUnit.MINUTES);
    }

    // 인증번호 검증
    public boolean verifyCode(String email, String code) {
        String authCode = "AuthCode:" + email;
        String storedCode = redisTemplate.opsForValue().get(authCode);
        if (storedCode != null && storedCode.equals(code)) {
            return true;
        }
        return false;
    }

    private String createRandomCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}