package com.nhnacademy.memberapi.domain.address.repository;

import com.nhnacademy.memberapi.domain.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
