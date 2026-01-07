package com.nhnacademy.memberapi.domain.address.controller.docs;

import com.nhnacademy.memberapi.domain.address.dto.AddressCreateRequest;
import com.nhnacademy.memberapi.domain.address.dto.AddressResponse;
import com.nhnacademy.memberapi.domain.address.dto.AddressUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Tag(name = "Address", description = "배송지 주소 관련 API")
public interface AddressControllerDocs {

    @Operation(summary = "주소 등록", description = "회원의 배송지 주소를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "주소 등록 성공")
    ResponseEntity<Void> addAddress(
            @Parameter(description = "회원 ID", required = true) @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody AddressCreateRequest request
    );

    @Operation(summary = "전체 주소 조회", description = "회원의 등록된 모든 배송지 주소를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "전체 주소 조회 성공")
    ResponseEntity<List<AddressResponse>> getAllAddresses(
            @Parameter(description = "회원 ID", required = true) @RequestHeader("X-Member-Id") Long memberId
    );

    @Operation(summary = "단건 주소 조회", description = "특정 배송지 주소를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "주소 조회 성공")
    ResponseEntity<AddressResponse> getAddress(
            @Parameter(description = "회원 ID", required = true) @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "주소 ID", required = true) @PathVariable("addressId") Long addressId
    );

    @Operation(summary = "주소 수정", description = "등록된 배송지 주소를 수정합니다.")
    @ApiResponse(responseCode = "204", description = "주소 수정 성공")
    ResponseEntity<Void> updateAddress(
            @Parameter(description = "회원 ID", required = true) @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "주소 ID", required = true) @PathVariable Long addressId,
            @Valid @RequestBody AddressUpdateRequest request
    );

    @Operation(summary = "주소 삭제", description = "등록된 배송지 주소를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "주소 삭제 성공")
    ResponseEntity<Void> deleteAddress(
            @Parameter(description = "회원 ID", required = true) @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "주소 ID", required = true) @PathVariable("addressId") Long addressId
    );
}
