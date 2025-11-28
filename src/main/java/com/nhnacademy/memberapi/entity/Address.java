package com.nhnacademy.memberapi.entity;

import com.nhnacademy.memberapi.dto.request.AddressCreateRequest;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Address")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "address_post_code", nullable = false, length = 255)
    private String addressPostCode;

    @Column(name = "address_base", nullable = false, length = 255)
    private String addressBase;

    @Column(name = "address_detail", nullable = false, length = 255)
    private String addressDetail;

    @Column(name = "address_alias", nullable = false, length = 255)
    private String addressAlias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void update(String postCode, String base, String detail, String alias) {
        this.addressPostCode = postCode;
        this.addressBase = base;
        this.addressDetail = detail;
        this.addressAlias = alias;
    }

    public static Address fromDto(AddressCreateRequest dto) {
        return Address.builder()
                .addressPostCode(dto.addressPostCode())
                .addressBase(dto.addressBase())
                .addressDetail(dto.addressDetail())
                .addressAlias(dto.addressAlias())
                // member 필드는 null로 두고, Service에서 setMember(member)로 연결
                .build();
    }
}