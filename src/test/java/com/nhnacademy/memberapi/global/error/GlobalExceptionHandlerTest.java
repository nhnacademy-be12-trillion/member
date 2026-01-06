package com.nhnacademy.memberapi.global.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import com.nhnacademy.memberapi.global.error.exception.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // StandaloneSetup을 사용하여 TestController와 GlobalExceptionHandler만 직접 로드
        // -> Spring Context 로딩 문제 해결
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 예외를 강제로 발생시키기 위한 테스트용 컨트롤러
     */
    @RestController
    static class TestController {
        @GetMapping("/test/user-already-exists")
        void throwUserAlreadyExistsException() {
            throw new UserAlreadyExistsException("test@test.com");
        }

        @GetMapping("/test/duplicate-contact")
        void throwDuplicateMemberContactException() {
            throw new DuplicateMemberContactException("010-1234-5678");
        }

        @GetMapping("/test/duplicate-point")
        void throwDuplicatePointException() {
            throw new DuplicatePointException("이미 적립됨");
        }

        @GetMapping("/test/user-not-found")
        void throwUserNotFoundException() {
            throw new UserNotFoundException("User not found");
        }

        @GetMapping("/test/member-not-found")
        void throwMemberNotFoundException() {
            throw new MemberNotFoundException("Member not found");
        }

        @GetMapping("/test/point-history-not-found")
        void throwPointHistoryNotFoundException() {
            throw new PointHistoryNotFoundException("History not found");
        }

        @GetMapping("/test/grade-not-found")
        void throwGradeNotFoundException() {
            throw new GradeNotFoundException("Grade not found");
        }

        @GetMapping("/test/point-policy-not-found")
        void throwPointPolicyNotFoundException() {
            throw new PointPolicyNotFoundException("Policy not found");
        }

        @GetMapping("/test/address-not-found")
        void throwAddressNotFoundException() {
            throw new AddressNotFoundException("Address not found");
        }

        @GetMapping("/test/insufficient-points")
        void throwInsufficientPointsException() {
            throw new InsufficientPointsException("잔액 부족");
        }

        @GetMapping("/test/invalid-point-policy-type")
        void throwInvalidPointPolicyTypeException() {
            throw new InvalidPointPolicyTypeException("잘못된 타입");
        }

        @GetMapping("/test/email-send")
        void throwEmailSendException() {
            throw new EmailSendException("전송 실패");
        }

        @GetMapping("/test/invalid-code")
        void throwInvalidVerificationCodeException() {
            throw new InvalidVerificationCodeException("코드 불일치");
        }

        @GetMapping("/test/max-size")
        void throwAddressMaxSizeException() {
            throw new AddressMaxSizeException("최대 개수 초과");
        }

        @GetMapping("/test/member-state-conflict")
        void throwMemberStateConflictException() {
            throw new MemberStateConflictException("휴면 계정", MemberState.DORMANT);
        }

        @GetMapping("/test/access-denied")
        void throwAccessDeniedException() {
            throw new AccessDeniedException("접근 권한 없음");
        }

        @GetMapping("/test/invalid-value")
        void throwInvalidValueException() {
            throw new InvalidValueException("잘못된 값");
        }

        @GetMapping("/test/internal-error")
        void throwException() {
            throw new RuntimeException("알 수 없는 오류");
        }

        @PostMapping("/test/validation")
        void throwValidationException(@Valid @RequestBody TestRequest request) {
            // Valid에 걸려서 진입하지 않음
        }
    }

    // 유효성 검사용 DTO
    static class TestRequest {
        @NotBlank(message = "필수값입니다.")
        public String name;

        public TestRequest() {}
        public TestRequest(String name) { this.name = name; }
        public String getName() { return name; }
    }

    @Test
    @DisplayName("UserAlreadyExistsException - 409 Conflict")
    void handleUserAlreadyExistsException() throws Exception {
        mockMvc.perform(get("/test/user-already-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("User Already Exists"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("DuplicateMemberContactException - 409 Conflict")
    void handleDuplicateMemberContactException() throws Exception {
        mockMvc.perform(get("/test/duplicate-contact"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate Member Info"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("DuplicatePointException - 409 Conflict")
    void handleDuplicatePointException() throws Exception {
        mockMvc.perform(get("/test/duplicate-point"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate Point Earned"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("UserNotFoundException - 404 Not Found")
    void handleUserNotFoundException() throws Exception {
        mockMvc.perform(get("/test/user-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("User Not Found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("MemberNotFoundException - 404 Not Found")
    void handleMemberNotFoundException() throws Exception {
        mockMvc.perform(get("/test/member-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Member Not Found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PointHistoryNotFoundException - 404 Not Found")
    void handlePointHistoryNotFoundException() throws Exception {
        mockMvc.perform(get("/test/point-history-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Point History Not Found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GradeNotFoundException - 404 Not Found")
    void handleGradeNotFoundException() throws Exception {
        mockMvc.perform(get("/test/grade-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Grade Not Found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PointPolicyNotFoundException - 404 Not Found")
    void handlePointPolicyNotFoundException() throws Exception {
        mockMvc.perform(get("/test/point-policy-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Point Policy Not Found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("AddressNotFoundException - 404 Not Found")
    void handleAddressNotFoundException() throws Exception {
        mockMvc.perform(get("/test/address-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Address Not Found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("InsufficientPointsException - 400 Bad Request")
    void handleInsufficientPointsException() throws Exception {
        mockMvc.perform(get("/test/insufficient-points"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Insufficient Points"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("InvalidPointPolicyTypeException - 400 Bad Request")
    void handleInvalidPointPolicyTypeException() throws Exception {
        mockMvc.perform(get("/test/invalid-point-policy-type"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Point Policy Type"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("EmailSendException - 400 Bad Request")
    void handleEmailSendException() throws Exception {
        mockMvc.perform(get("/test/email-send"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Email Send Failed"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("InvalidVerificationCodeException - 400 Bad Request")
    void handleInvalidVerificationCodeException() throws Exception {
        mockMvc.perform(get("/test/invalid-code"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Verification Code"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("AddressMaxSizeException - 400 Bad Request")
    void handleMaxSizeException() throws Exception {
        mockMvc.perform(get("/test/max-size"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Max Size Exceeded"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("MemberStateConflictException - 403 Forbidden")
    void handleMemberStateConflictException() throws Exception {
        mockMvc.perform(get("/test/member-state-conflict"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Account Suspended"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("AccessDeniedException - 403 Forbidden")
    void handleAccessDeniedException() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access Denied"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("InvalidValueException - 400 Bad Request")
    void handleInvalidValueException() throws Exception {
        mockMvc.perform(get("/test/invalid-value"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Value"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Generic Exception - 500 Internal Server Error")
    void handleGlobalException() throws Exception {
        mockMvc.perform(get("/test/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException - 400 Bad Request")
    void handleValidationExceptions() throws Exception {
        // given: name이 비어있는(Invalid) JSON 요청
        String invalidJson = "{\"name\": \"\"}";

        // when & then
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").value("필수값입니다."));
    }
}