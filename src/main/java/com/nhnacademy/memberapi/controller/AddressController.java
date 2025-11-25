package com.nhnacademy.memberapi.controller;

import com.nhnacademy.memberapi.dto.CustomUserDetails;
import com.nhnacademy.memberapi.dto.request.AddressCreateRequest;
import com.nhnacademy.memberapi.dto.request.AddressUpdateRequest;
import com.nhnacademy.memberapi.dto.response.AddressResponse;
import com.nhnacademy.memberapi.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // 주소 등록
    @PostMapping
    public ResponseEntity<Void> addAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddressCreateRequest request
    ) {
        addressService.addAddress(userDetails.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 전체 주소 조회
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAllAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        List<AddressResponse> addresses = addressService.getAllAddresses(userDetails.getMemberId());
        return ResponseEntity.status(HttpStatus.OK).body(addresses);
    }

    // 주소 조회
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("addressId") Long addressId
    ){
        AddressResponse address = addressService.getAddress(userDetails.getMemberId(), addressId);
        return ResponseEntity.status(HttpStatus.OK).body(address);
    }

    // 주소 수정
    @PutMapping
    public ResponseEntity<Void> updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddressUpdateRequest request
    ) {
        addressService.updateAddress(userDetails.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 주소 삭제
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("addressId") Long addressId
    ){
        addressService.deleteAddress(userDetails.getMemberId(), addressId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}