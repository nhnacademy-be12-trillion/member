package com.nhnacademy.memberapi.domain.address.service;

import com.nhnacademy.memberapi.domain.address.dto.AddressCreateRequest;
import com.nhnacademy.memberapi.domain.address.dto.AddressResponse;
import com.nhnacademy.memberapi.domain.address.dto.AddressUpdateRequest;
import com.nhnacademy.memberapi.domain.address.entity.Address;
import com.nhnacademy.memberapi.domain.address.repository.AddressRepository;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import com.nhnacademy.memberapi.global.error.exception.AccessDeniedException;
import com.nhnacademy.memberapi.global.error.exception.AddressNotFoundException;
import com.nhnacademy.memberapi.global.error.exception.MaxSizeException;
import com.nhnacademy.memberapi.global.error.exception.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;

    // 주소 추가 (최대 10개)
    public void addAddress(Long memberId, @Valid AddressCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("Member not found"));

        if (member.getAddresses().size() >= 10) {
            throw new MaxSizeException("주소는 최대 10개까지만 등록할 수 있습니다.");
        }

        Address address = Address.builder()
                .addressPostCode(request.addressPostCode())
                .addressBase(request.addressBase())
                .addressDetail(request.addressDetail())
                .addressAlias(request.addressAlias())
                .member(member)
                .build();

        member.getAddresses().add(address);
        addressRepository.save(address);
    }

    // 전체 주소 조회
    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddresses(Long memberId) {
        if(!memberRepository.existsByMemberId(memberId)){
            throw new UserNotFoundException("Member not found");
        }

        return addressRepository.findAllByMember_MemberId(memberId);
    }

    // 주소 조회
    @Transactional(readOnly = true)
    public AddressResponse getAddress(Long memberId, Long addressId) {
        Address address = addressRepository.findByAddressIdAndMember_MemberId(memberId, addressId)
                .orElseThrow(() -> new AddressNotFoundException("주소를 찾을 수 없습니다. " + addressId));

        return AddressResponse.fromEntity(address);
    }


    // 주소 수정
    public void updateAddress(Long memberId, Long addressId, @Valid AddressUpdateRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException("주소를 찾을 수 없습니다. " + addressId));

        // 삭제 요청자의 주소가 맞는지 확인
        if (!address.getMember().getMemberId().equals(memberId)) {
            throw new AccessDeniedException("이 주소를 수정할 권한이 없습니다.");
        }

        address.update(
                request.addressPostCode(),
                request.addressBase(),
                request.addressDetail(),
                request.addressAlias()
        );
    }

    // 주소 삭제
    public void deleteAddress(Long memberId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException("주소를 찾을 수 없습니다. " + addressId));

        // 삭제 요청자의 주소가 맞는지 확인
        if (!address.getMember().getMemberId().equals(memberId)) {
            throw new AccessDeniedException("이 주소를 삭제할 권한이 없습니다.");
        }

        addressRepository.delete(address);
    }
}