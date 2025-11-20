package com.nhnacademy.memberapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Address")
@Getter
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
    @JoinColumn(name = "member_id", nullable = false) // FK 컬럼명
    private Member member;

    public void update(String postCode, String base, String detail, String alias) {
        this.addressPostCode = postCode;
        this.addressBase = base;
        this.addressDetail = detail;
        this.addressAlias = alias;
    }
}