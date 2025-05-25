package com.realive.repository.admin.approval;

import com.realive.domain.seller.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Seller, Long>, JpaSpecificationExecutor<Seller> {

    /**
     * 승인 대기 중인 판매자 목록을 조회합니다.
     * 조건: isApproved = false AND approvedAt IS NULL
     * @return 승인 대기 중인 판매자 리스트
     */
    List<Seller> findByIsApprovedFalseAndApprovedAtIsNull(); // 승인대기 판매자 조회


}
