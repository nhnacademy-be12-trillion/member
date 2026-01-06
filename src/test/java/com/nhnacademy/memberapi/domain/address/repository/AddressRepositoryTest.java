package com.nhnacademy.memberapi.domain.address.repository;

import com.nhnacademy.memberapi.domain.address.dto.AddressResponse;
import com.nhnacademy.memberapi.domain.address.entity.Address;
import com.nhnacademy.memberapi.domain.grade.entity.Grade;
import com.nhnacademy.memberapi.domain.grade.entity.GradeName;
import com.nhnacademy.memberapi.domain.member.entity.Member;
import com.nhnacademy.memberapi.domain.member.entity.MemberRole;
import com.nhnacademy.memberapi.domain.member.entity.MemberState;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
})
class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private EntityManager em;

    private Member member;
    private Member otherMember;
    private Address address1;
    private Address address2;

    @BeforeEach
    void setUp() {
        Grade grade = Grade.builder()
                .gradeName(GradeName.COMMON)
                .gradePointRatio(BigDecimal.valueOf(0.1))
                .gradeCondition(0)
                .build();
        em.persist(grade);

        member = Member.builder()
                .memberEmail("test@nhn.com")
                .memberPassword("password123!")
                .memberName("TestUser")
                .memberContact("010-1111-2222")
                .memberBirth(LocalDate.of(1990, 1, 1))
                .memberState(MemberState.ACTIVE)
                .memberLatestLoginAt(LocalDate.now())
                .memberRole(MemberRole.MEMBER)
                .memberPoint(0)
                .memberAccumulateAmount(0)
                .grade(grade)
                .build();
        em.persist(member);

        otherMember = Member.builder()
                .memberEmail("other@nhn.com")
                .memberPassword("password123!")
                .memberName("OtherUser")
                .memberContact("010-3333-4444")
                .memberBirth(LocalDate.of(1995, 5, 5))
                .memberState(MemberState.ACTIVE)
                .memberLatestLoginAt(LocalDate.now())
                .memberRole(MemberRole.MEMBER)
                .memberPoint(0)
                .memberAccumulateAmount(0)
                .grade(grade)
                .build();
        em.persist(otherMember);

        address1 = Address.builder()
                .addressPostCode("12345")
                .addressBase("서울시 강남구")
                .addressDetail("101호")
                .addressAlias("우리집")
                .member(member)
                .build();
        em.persist(address1);

        address2 = Address.builder()
                .addressPostCode("67890")
                .addressBase("경기도 성남시")
                .addressDetail("202호")
                .addressAlias("회사")
                .member(member)
                .build();
        em.persist(address2);

        Address otherAddress = Address.builder()
                .addressPostCode("11111")
                .addressBase("제주시")
                .addressDetail("303호")
                .addressAlias("별장")
                .member(otherMember)
                .build();
        em.persist(otherAddress);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("특정 회원의 모든 주소를 DTO 리스트로 조회한다")
    void findAllByMember_MemberId() {
        List<AddressResponse> results = addressRepository.findAllByMember_MemberId(member.getMemberId());

        assertThat(results).hasSize(2);
        assertThat(results).extracting("addressAlias")
                .containsExactlyInAnyOrder("우리집", "회사");

        assertThat(results).extracting("addressPostCode")
                .contains("12345", "67890");
    }

    @Test
    @DisplayName("특정 회원의 특정 주소 ID 조회 - 성공")
    void findByMember_MemberIdAndAddressId_Success() {
        Optional<Address> result = addressRepository.findByMember_MemberIdAndAddressId(member.getMemberId(), address1.getAddressId());

        assertThat(result).isPresent();
        assertThat(result.get().getAddressBase()).isEqualTo("서울시 강남구");
        assertThat(result.get().getMember().getMemberId()).isEqualTo(member.getMemberId());
    }

    @Test
    @DisplayName("특정 회원의 특정 주소 ID 조회 - 실패 (내 주소가 아님)")
    void findByMember_MemberIdAndAddressId_Fail_NotMyAddress() {
        Optional<Address> result = addressRepository.findByMember_MemberIdAndAddressId(otherMember.getMemberId(), address1.getAddressId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("특정 회원의 특정 주소 ID 조회 - 실패 (존재하지 않는 주소 ID)")
    void findByAddressIdAndMember_MemberId_Fail_InvalidAddressIdAndAddressId() {
        // when
        Optional<Address> result = addressRepository.findByMember_MemberIdAndAddressId(member.getMemberId(), 9999L);

        // then
        assertThat(result).isEmpty();
    }
}