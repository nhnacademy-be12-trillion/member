package com.nhnacademy.memberapi.repository;

import com.nhnacademy.memberapi.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByMemberEmail(String memberEmail);
    Optional<Member> findByMemberEmail(String memberEmail);


    Optional<Member> findByMemberNameAndMemberContact(String memberName, String memberContact);
}
