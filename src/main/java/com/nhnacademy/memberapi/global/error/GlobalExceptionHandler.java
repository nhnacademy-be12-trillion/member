package com.nhnacademy.memberapi.global.error;

import com.nhnacademy.memberapi.global.error.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 409 Conflict Error (회원가입 시 이미 존재하는 이메일일 경우)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        log.warn("회원가입 실패 - 이미 존재하는 사용자: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "User Already Exists",
                HttpStatus.CONFLICT.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // 409 Conflict Error (중복된 회원 정보 - 전화번호 등)
    @ExceptionHandler(DuplicateMemberContactException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateMemberContactException(DuplicateMemberContactException e) {
        log.warn("중복된 회원 정보: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Duplicate Member Info",
                HttpStatus.CONFLICT.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // 409 Conflict Error (이미 적립된 포인트/리뷰 중복 적립 시도)
    @ExceptionHandler(DuplicatePointException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePointException(DuplicatePointException e) {
        log.warn("중복된 포인트 적립 시도: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Duplicate Point Earned",
                HttpStatus.CONFLICT.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // 404 Not Found Error (Spring Security 관련 사용자를 찾을 수 없는 경우)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("사용자를 찾을 수 없음(Security): {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "User Not Found",
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 404 Not Found Error (비즈니스 로직 상 회원을 찾을 수 없는 경우)
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMemberNotFoundException(MemberNotFoundException e) {
        log.warn("회원을 찾을 수 없음: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Member Not Found",
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 404 Not Found Error (포인트 내역을 찾을 수 없는 경우 - 환불 등)
    @ExceptionHandler(PointHistoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePointHistoryNotFoundException(PointHistoryNotFoundException e) {
        log.warn("포인트 내역을 찾을 수 없음: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Point History Not Found",
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 404 Not Found Error (등급을 찾을 수 없는 경우)
    @ExceptionHandler(GradeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGradeNotFoundException(GradeNotFoundException e){
        log.warn("등급을 찾을 수 없습니다: {}",e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Grade Not Found",
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 404 Not Found Error (포인트 정책을 찾을 수 없는 경우)
    @ExceptionHandler(PointPolicyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePolicyNotFoundException(PointPolicyNotFoundException e){
        log.warn("포인트 정책을 찾을 수 없습니다: {}",e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Point Policy Not Found",
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 404 Not Found Error (주소 정보를 찾을 수 없는 경우)
    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAddressNotFoundException(AddressNotFoundException e) {
        log.warn("주소 정보를 찾을 수 없음: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Address Not Found",
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 400 Bad Request Error (@Valid 유효성 검사 실패 시)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        log.info("유효성 검사 실패: {}", e.getBindingResult());
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

    // 400 Bad Request Error (포인트 잔액 부족)
    @ExceptionHandler(InsufficientPointsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPointsException(InsufficientPointsException e) {
        log.warn("포인트 잔액 부족: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Insufficient Points",
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 400 Bad Request Error (잘못된 포인트 정책 타입 요청)
    @ExceptionHandler(InvalidPointPolicyTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPointPolicyTypeException(InvalidPointPolicyTypeException e) {
        log.warn("유효하지 않은 포인트 정책 타입: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Invalid Point Policy Type",
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 400 Bad Request Error (이메일 전송 오류)
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendException(EmailSendException e) {
        log.error("이메일 전송 중 오류 발생: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Email Send Failed",
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 400 Bad Request Error (인증번호 인증 오류)
    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationCodeException(InvalidVerificationCodeException e) {
        log.warn("인증번호 검증 실패: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Invalid Verification Code",
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 400 Bad Request Error (주소 최대 등록 개수 초과)
    @ExceptionHandler(AddressMaxSizeException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(AddressMaxSizeException e) {
        log.warn("허용된 최대 개수 초과: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Max Size Exceeded",
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 403 Forbidden Error (탈퇴한 회원, 휴면 계정 등 로그인은 성공했으나 접근이 불가능한 경우)
    @ExceptionHandler(MemberStateConflictException.class)
    public ResponseEntity<ErrorResponse> handleMemberStateConflict(MemberStateConflictException e) {
        log.warn("계정 상태 문제로 접근 거부: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Account Suspended",
                HttpStatus.FORBIDDEN.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // 403 Forbidden Error (접근 권한이 없는 경우)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("접근 권한 없음: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Access Denied",
                HttpStatus.FORBIDDEN.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // 400 Bad Request Error (올바르지 않은 값으로 수정하려는 경우)
    @ExceptionHandler(InvalidValueException.class)
    public ResponseEntity<ErrorResponse> handlerInvalidValueException(InvalidValueException e){
        log.warn("올바르지 않은 값입니다. {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                "Invalid Value",
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 500 Internal Server Error (그 외 모든 예외)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e) {
        log.error("Internal Server Error", e);
        ErrorResponse response = ErrorResponse.of(
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "서버 내부 오류가 발생했습니다: " + e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}