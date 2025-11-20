package com.nhnacademy.memberapi.controller;

import com.nhnacademy.memberapi.dto.CustomUserDetails;
import com.nhnacademy.memberapi.dto.request.AddressCreateRequest;
import com.nhnacademy.memberapi.dto.request.AddressUpdateRequest;
import com.nhnacademy.memberapi.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// todo 예외 처리
@RestController
@RequestMapping("/members/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<Void> addAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddressCreateRequest request
    ) {
        addressService.addAddress(userDetails.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddressUpdateRequest request
    ) {
        addressService.updateAddress(userDetails.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}