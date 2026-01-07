package com.nhnacademy.memberapi.domain.address.controller;

import com.nhnacademy.memberapi.domain.address.controller.docs.AddressControllerDocs;
import com.nhnacademy.memberapi.domain.address.dto.AddressCreateRequest;
import com.nhnacademy.memberapi.domain.address.dto.AddressResponse;
import com.nhnacademy.memberapi.domain.address.dto.AddressUpdateRequest;
import com.nhnacademy.memberapi.domain.address.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members/addresses")
@RequiredArgsConstructor
public class AddressController implements AddressControllerDocs {

    private final AddressService addressService;

    // 주소 등록
    @PostMapping
    public ResponseEntity<Void> addAddress(
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody AddressCreateRequest request
    ) {
        addressService.addAddress(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 전체 주소 조회
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAllAddresses(
            @RequestHeader("X-Member-Id") Long memberId
    ){
        List<AddressResponse> addresses = addressService.getAllAddresses(memberId);
        return ResponseEntity.status(HttpStatus.OK).body(addresses);
    }

    // 주소 조회
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddress(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable("addressId") Long addressId
    ){
        AddressResponse address = addressService.getAddress(memberId, addressId);
        return ResponseEntity.status(HttpStatus.OK).body(address);
    }

    // 주소 수정
    @PutMapping("/{addressId}")
    public ResponseEntity<Void> updateAddress(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressUpdateRequest request
    ) {
        addressService.updateAddress(memberId, addressId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 주소 삭제
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable("addressId") Long addressId
    ){
        addressService.deleteAddress(memberId, addressId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}