package com.nhnacademy.memberapi.domain.address.service;

import com.nhnacademy.memberapi.domain.address.dto.AddressCreateRequest;
import com.nhnacademy.memberapi.domain.address.dto.AddressResponse;
import com.nhnacademy.memberapi.domain.address.dto.AddressUpdateRequest;
import com.nhnacademy.memberapi.domain.address.entity.Address;
import com.nhnacademy.memberapi.domain.address.repository.AddressRepository;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.repository.MemberRepository;
import com.nhnacademy.memberapi.global.error.exception.AddressNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @InjectMocks
    private AddressService addressService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member createMember(Long memberId) {
        return Member.builder().memberId(memberId).memberEmail("test@gmail.com").build();
    }

    private Address createAddress(Long addressId, Member member) {
        return Address.builder()
                .addressId(addressId)
                .addressPostCode("12345")
                .addressBase("기본주소")
                .addressDetail("상세주소")
                .addressAlias("잡")
                .member(member)
                .build();
    }

    @Test
    @DisplayName("주소 등록 성공")
    void addAddress() {
        Long memberId = 1L;
        AddressCreateRequest request = new AddressCreateRequest("12345", "기본", "상세", "집");
        Member member = createMember(memberId);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        addressService.addAddress(memberId, request);

        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("전체 주소 조회")
    void getAllAddresses() {
        Long memberId = 1L;
        AddressResponse addressResponse = new AddressResponse(1L,"12345","기본주소","상세주소","집");
        List<AddressResponse> addresses = List.of(addressResponse);

        given(memberRepository.existsByMemberId(memberId)).willReturn(true);
        given(addressRepository.findAllByMember_MemberId(memberId)).willReturn(addresses);

        List<AddressResponse> result = addressService.getAllAddresses(memberId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).addressAlias()).isEqualTo("집");
    }

    @Test
    @DisplayName("주소 단건 조회")
    void getAddressSuccess() {
        Long memberId = 1L;
        Long addressId = 10L;
        Member member = createMember(memberId);
        Address address = createAddress(addressId, member);

        given(addressRepository.findByMember_MemberIdAndAddressId(memberId, addressId)).willReturn(Optional.of(address));

        AddressResponse result = addressService.getAddress(memberId, addressId);

        assertThat(result.addressId()).isEqualTo(addressId);
    }

    @Test
    @DisplayName("주소 조회 실패 (등록되지 않은 주소 조회 시도)")
    void getAddressFail() {
        Long myMemberId = 1L;
        Long addressId = 10L;

        given(addressRepository.findByMember_MemberIdAndAddressId(myMemberId, addressId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddress(myMemberId, addressId))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    @DisplayName("주소 수정")
    void updateAddress() {
        Long memberId = 1L;
        Long addressId = 10L;
        AddressUpdateRequest request = new AddressUpdateRequest("54321", "새주소", "새상세", "회사");

        Member member = createMember(memberId);
        Address address = createAddress(addressId, member);

        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

        addressService.updateAddress(memberId, addressId, request);

        assertThat(address.getAddressBase()).isEqualTo("새주소");
        assertThat(address.getAddressAlias()).isEqualTo("회사");
    }

    @Test
    @DisplayName("주소 삭제")
    void deleteAddress() {
        Long memberId = 1L;
        Long addressId = 10L;
        Member member = createMember(memberId);
        Address address = createAddress(addressId, member);

        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

        addressService.deleteAddress(memberId, addressId);

        verify(addressRepository).delete(address);
    }
}