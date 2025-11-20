package com.nhnacademy.memberapi.service;

import com.nhnacademy.memberapi.dto.request.AddressCreateRequest;
import com.nhnacademy.memberapi.dto.request.AddressUpdateRequest;
import com.nhnacademy.memberapi.entity.Address;
import com.nhnacademy.memberapi.entity.Member;
import com.nhnacademy.memberapi.repository.AddressRepository;
import com.nhnacademy.memberapi.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;

    // 주소 추가 (최대 10개)
    public void addAddress(Long memberId, @Valid AddressCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found"));

        if (member.getAddresses().size() >= 10) {
            throw new IllegalStateException("주소는 최대 10개까지만 등록할 수 있습니다.");
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

    public void updateAddress(Long memberId, @Valid AddressUpdateRequest request) {
        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new UsernameNotFoundException("Address not found: " + request.addressId()));

        if (!address.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("이 주소를 수정할 권한이 없습니다.");
        }

        address.update(
                request.addressPostCode(),
                request.addressBase(),
                request.addressDetail(),
                request.addressAlias()
        );
    }
}