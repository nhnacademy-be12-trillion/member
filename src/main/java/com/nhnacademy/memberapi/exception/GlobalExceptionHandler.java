package com.nhnacademy.memberapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.nhnacademy.memberapi.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 409 Conflict Error (회원가입 시 이미 존재하는 이메일일 경우)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        ErrorResponse response = ErrorResponse.of(
                "User Already Exists",
                HttpStatus.CONFLICT.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // 404 Not Found Error (회원을 찾을 수 없는 경우)
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException e) {
        ErrorResponse response = ErrorResponse.of(
                "User Not Found",
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    // 400 Bad Request Error (@Valid 유효성 검사 실패 시)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse response = ErrorResponse.of(
                "Validation Failed",
                HttpStatus.BAD_REQUEST.value(),
                "입력값이 유효하지 않습니다.",
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 401 Unauthorized Error (리프레시 토큰 유효성 검사 실패 시)
    // todo 클라이언트는 이 응답을 받으면 로그인 페이지로 이동해야 함
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException e) {
        ErrorResponse response = ErrorResponse.of(
                "Invalid Refresh Token",
                HttpStatus.UNAUTHORIZED.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // 403 Forbidden Error (탈퇴한 회원, 휴면 계정 등 로그인은 성공했으나 접근이 불가능한 경우)
    @ExceptionHandler(MemberStateConflictException.class)
    public ResponseEntity<ErrorResponse> handleMemberStateConflict(MemberStateConflictException e) {
        ErrorResponse response = ErrorResponse.of(
                "Account Suspended",
                HttpStatus.FORBIDDEN.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // 400 Bad Request Error (이메일 전송 오류)
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendException(EmailSendException e) {
        ErrorResponse response = ErrorResponse.of(
                "이메일 전송 실패",
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 400 Bad Request Error (인증번호 인증 오류)
    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationCodeException(InvalidVerificationCodeException e) {
        ErrorResponse response = ErrorResponse.of(
                "인증번호가 일치하지 않거나 만료되었습니다.",
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 500 Internal Server Error (그 외 모든 예외)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e) {
        // 실제 운영에서는 e.printStackTrace() 대신 로거(log.error)를 사용하세요.
        ErrorResponse response = ErrorResponse.of(
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "서버 내부 오류가 발생했습니다: " + e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
