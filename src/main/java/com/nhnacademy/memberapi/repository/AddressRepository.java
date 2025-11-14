package com.nhnacademy.memberapi.repository;

import com.nhnacademy.memberapi.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
