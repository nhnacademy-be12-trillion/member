package com.nhnacademy.memberapi.domain.member.service;

import com.nhnacademy.memberapi.global.error.exception.EmailSendException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("인증번호 생성 및 이메일 전송")
    void sendVerificationCode() {
        String email = "test@nhn.com";
        String redisKey = "AuthCode:" + email;

        emailService.sendVerificationCode(email);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly(email);
        assertThat(sentMessage.getSubject()).isEqualTo("Trillion 인증번호");

        String body = sentMessage.getText();
        assertThat(body).isNotNull();
        String codeInMail = body.replaceAll("[^0-9]", "");
        assertThat(codeInMail).hasSize(6);

        verify(valueOperations).set(eq(redisKey), eq(codeInMail), eq(3L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("이메일 전송 실패 시 예외 발생")
    void sendVerificationCode_Fail() {
        String email = "test@nhn.com";
        doThrow(new MailSendException("Mail Server Error")).when(javaMailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.sendVerificationCode(email))
                .isInstanceOf(EmailSendException.class)
                .hasMessage("이메일 전송 실패");

        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("인증번호 검증")
    void verifyCode() {
        String email = "test@nhn.com";
        String code = "123456";
        String redisKey = "AuthCode:" + email;

        given(valueOperations.get(redisKey)).willReturn(code);

        boolean result = emailService.verifyCode(email, code);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("인증번호 검증 실패 - 불일치")
    void verifyCode_Fail_WrongCode() {
        String email = "test@nhn.com";
        String correctCode = "123456";
        String wrongInput = "999999";
        String redisKey = "AuthCode:" + email;

        given(valueOperations.get(redisKey)).willReturn(correctCode);

        boolean result = emailService.verifyCode(email, wrongInput);

        assertThat(result).isFalse();
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("인증번호 검증 실패 - 만료됨 (Redis Key 없음)")
    void verifyCode_Fail_Expired() {
        String email = "test@nhn.com";
        String code = "123456";
        String redisKey = "AuthCode:" + email;

        given(valueOperations.get(redisKey)).willReturn(null);

        boolean result = emailService.verifyCode(email, code);

        assertThat(result).isFalse();
        verify(redisTemplate, never()).delete(anyString());
    }
}