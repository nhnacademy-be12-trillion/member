package com.nhnacademy.memberapi.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Entity
@Table(name = "Member")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    // 기본 키 autoincrement
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "member_email", nullable = false, unique = true, length = 255)
    private String memberEmail;

    @Column(name = "member_password", nullable = false, length = 255)
    private String memberPassword;

    @Column(name = "member_name", nullable = false, length = 255)
    private String memberName;

    @Column(name = "member_contact", nullable = false, length = 255)
    private String memberContact;

    @Column(name = "member_birth", nullable = false)
    private LocalDate memberBirth;

    @Column(name = "member_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberState memberState;

    @Column(name = "member_latest_login_at", nullable = false)
    private LocalDate memberLastestLoginAt;

    @Column(name = "member_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRole memberRole;

    @Column(name = "member_point", nullable = false)
    private Integer memberPoint;

    @Column(name = "member_accumulate_amount", nullable = false)
    private Integer memberAccumulateAmount;

    @Column(name = "member_oauth_id", length = 255)
    private String memberOauthId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    public static Member createForAuthentication(Long memberId, MemberRole role){
        Member member = new Member();
        member.setMemberId(memberId);
        member.setMemberEmail("jwt@temp.com"); // 사용되지 않을 임시 이메일
        member.setMemberPassword("temppassword"); // 사용되지 않을 임시 비밀번호
        member.setMemberRole(role);
        return member;
    }
}