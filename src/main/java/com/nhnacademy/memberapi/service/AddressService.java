package com.nhnacademy.memberapi.service;

import com.nhnacademy.memberapi.dto.request.AddressCreateRequest;
import com.nhnacademy.memberapi.dto.request.AddressUpdateRequest;
import com.nhnacademy.memberapi.dto.response.AddressResponse;
import com.nhnacademy.memberapi.entity.Address;
import com.nhnacademy.memberapi.entity.Member;
import com.nhnacademy.memberapi.repository.AddressRepository;
import com.nhnacademy.memberapi.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    // 전체 주소 조회
    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddresses(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found"));

        // Member 엔티티의 addresses 필드 활용 (Lazy Loading이 걸려있으므로 트랜잭션 필요)
        return member.getAddresses().stream()
                .map(AddressResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 주소 조회
    @Transactional(readOnly = true)
    public AddressResponse getAddress(Long memberId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new UsernameNotFoundException("Address not found: " + addressId));

        if (!address.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("이 주소를 조회할 권한이 없습니다.");
        }

        return AddressResponse.fromEntity(address);
    }


    // 주소 수정
    public void updateAddress(Long memberId, @Valid AddressUpdateRequest request) {
        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new UsernameNotFoundException("Address not found: " + request.addressId()));

        // 삭제 요청자의 주소가 맞는지 확인
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

    // 주소 삭제
    public void deleteAddress(Long memberId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new UsernameNotFoundException("Address not found: " + addressId));

        // 삭제 요청자의 주소가 맞는지 확인
        if (!address.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("이 주소를 삭제할 권한이 없습니다.");
        }

        addressRepository.delete(address);
    }
}