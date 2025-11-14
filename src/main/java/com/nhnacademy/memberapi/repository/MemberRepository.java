package com.nhnacademy.memberapi.repository;

import com.nhnacademy.memberapi.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByMemberEmail(String memberEmail);
}
