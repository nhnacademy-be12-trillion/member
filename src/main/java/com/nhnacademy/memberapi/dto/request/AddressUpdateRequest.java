package com.nhnacademy.memberapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressUpdateRequest(
        // 수정할 주소를 식별하기 위해서
        @NotNull
        Long addressId,
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