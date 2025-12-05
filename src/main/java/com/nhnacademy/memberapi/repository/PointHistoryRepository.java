package com.nhnacademy.memberapi.repository;

import com.nhnacademy.memberapi.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    // 회원 ID와 리뷰ID로 지급 내역이 있는지 확인하기 위해서
    boolean existsByReviewId(Long reviewId);
    // 포인트 이력 조회 (mypage)
    List<PointHistory> findAllByMember_MemberIdOrderByTransactionAtDesc(Long memberId);
    List<PointHistory> findAllByMember_MemberIdAndOrderId(Long memberId, Long orderId);
}
