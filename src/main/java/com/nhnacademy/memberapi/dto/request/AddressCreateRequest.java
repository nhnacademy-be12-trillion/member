package com.nhnacademy.memberapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddressCreateRequest(
        // 우편 번호
        @NotBlank
        String addressPostCode,
        // 도로명 주소
        @NotBlank
        String addressBase,
        // 상세 주소
        @NotBlank
        String addressDetail,
        // 별칭
        @NotBlank 
        String addressAlias
) {}