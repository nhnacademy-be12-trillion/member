package com.nhnacademy.memberapi.service;

import com.nhnacademy.memberapi.exception.EmailSendException;
import com.nhnacademy.memberapi.exception.UserAlreadyExistsException;
import com.nhnacademy.memberapi.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberRepository;

    public void sendVerificationCode(String email) {
        // 중복 가입 체크
         if (memberRepository.existsByMemberEmail(email)){
             throw new UserAlreadyExistsException("이미 가입된 이메일입니다.");
         }

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
        // Redis에 저장. 유효기간 5분으로 설정 (Key: authCode, Value: 6자리 난수)
        redisTemplate.opsForValue().set(authCode, code, 5, TimeUnit.MINUTES);
    }

    // 인증번호 검증
    public boolean verifyCode(String email, String code) {
        String authCode = "AuthCode:" + email;
        String storedCode = redisTemplate.opsForValue().get(authCode);
        if (storedCode != null && storedCode.equals(code)) {
            // 인증 성공 시 재사용 방지를 위해 Redis에서 삭제
            redisTemplate.delete(authCode);
            return true;
        }
        return false;
    }

    private String createRandomCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}