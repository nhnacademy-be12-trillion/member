package com.nhnacademy.memberapi.domain.address.repository;

import com.nhnacademy.memberapi.domain.address.dto.AddressResponse;
import com.nhnacademy.memberapi.domain.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<AddressResponse> findAllByMember_MemberId(Long memberId);
    Optional<Address> findByMember_MemberIdAndAddressId(Long memberId, Long addressId);
}
