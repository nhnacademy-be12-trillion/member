package com.nhnacademy.memberapi.domain.member.repository;

import com.nhnacademy.memberapi.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByMemberEmail(String memberEmail);
    Optional<Member> findByMemberEmail(String memberEmail);
    Optional<Member> findByMemberNameAndMemberContact(String memberName, String memberContact);
    Optional<Member> findByMemberOauthId(String memberOauthId);
    Optional<Member> findByMemberContact(String memberContact);
    boolean existsByMemberId(Long memberId);
}

