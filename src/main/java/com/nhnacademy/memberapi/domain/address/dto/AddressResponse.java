package com.nhnacademy.memberapi.domain.address.dto;

import com.nhnacademy.memberapi.domain.address.entity.Address;

public record AddressResponse (
        Long addressId,
        String addressPostCode,
        String addressBase,
        String addressDetail,
        String addressAlias
) {
    public static AddressResponse fromEntity(Address address) {
        return new AddressResponse(
                address.getAddressId(),
                address.getAddressPostCode(),
                address.getAddressBase(),
                address.getAddressDetail(),
                address.getAddressAlias()
        );
    }
}