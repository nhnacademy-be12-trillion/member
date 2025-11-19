package com.nhnacademy.memberapi.repository;

import com.nhnacademy.memberapi.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByMemberEmail(String memberEmail);
    Optional<Member> findByMemberEmail(String memberEmail);
}
