package com.nhnacademy.memberapi.repository;

import com.nhnacademy.memberapi.entity.Member;
import com.nhnacademy.memberapi.entity.MemberState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByMemberEmail(String memberEmail);
    Optional<Member> findByMemberEmail(String memberEmail);

    // 휴면 처리 대상 조회(마지막 로그인 날짜가 기준일 이전이고, 상태가 ACTIVE인 회원)
    List<Member> findByMemberLastestLoginAtBeforeAndMemberState(LocalDate standardDate, MemberState state);
}
