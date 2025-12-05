package com.nhnacademy.memberapi.entity;

import com.nhnacademy.memberapi.exception.InsufficientPointsException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Entity
@Table(name = "Member")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    private static final String PHONE_REGEX = "^01(?:0|1|[2-9])-(?:\\d{3}|\\d{4})-\\d{4}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

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

    @Column(name = "member_contact", unique = true, length = 255)
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

    public void adjustPoint(int amount) {
        // 잔액이 음수가 되는 것을 방지
        if (this.memberPoint + amount < 0) {
            throw new InsufficientPointsException("포인트가 부족합니다.");
        }
        this.memberPoint += amount;
    }

    // 전화번호 저장 및 수정 전에 전화번호 정규화
    @PrePersist // Insert 직전 실행
    @PreUpdate  // Update 직전 실행
    public void normalizeMemberContact() {
        if (StringUtils.hasText(this.memberContact)) {
            // 숫자만 남기고 모두 제거 (010-1234-5678 -> 01012345678)
            String rawNumber = this.memberContact.replaceAll("[^0-9]", "");
            // 길이에 따라 하이픈 포맷 적용
            if (rawNumber.length() == 11) {
                // 01012345678 -> 010-1234-5678
                this.memberContact = String.format("%s-%s-%s",
                        rawNumber.substring(0, 3),
                        rawNumber.substring(3, 7),
                        rawNumber.substring(7));
            } else if (rawNumber.length() == 10) {
                // 0111234567 -> 011-123-4567 (구형 번호 대응)
                this.memberContact = String.format("%s-%s-%s",
                        rawNumber.substring(0, 3),
                        rawNumber.substring(3, 6),
                        rawNumber.substring(6));
            } else {
                // 길이가 이상하면 원본(숫자만 있는 상태) 그대로
                this.memberContact = rawNumber;
            }
        }
    }
}