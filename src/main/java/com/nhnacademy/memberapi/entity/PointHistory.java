package com.nhnacademy.memberapi.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PointHistory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pointhistory_id")
    private Long pointhistoryId;

    @Column(name = "pointhistory_reason", nullable = false, length = 100)
    private String pointhistoryReason;

    @Column(name = "pointhistory_point", nullable = false)
    private Integer pointhistoryPoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 주문 ID
    // 주문 서비스는 다른 도메인이므로, JPA 연관관계를 맺지 않고 ID 값만 저장한다.
    @Column(name = "sale_id", nullable = false)
    private Long saleId;
}