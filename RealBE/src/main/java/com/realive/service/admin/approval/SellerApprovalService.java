package com.realive.service.admin.approval;


import com.realive.dto.admin.approval.PendingSellerDTO;
import com.realive.dto.seller.SellerResponseDTO;

import java.util.List;

public interface SellerApprovalService {

    /**
     * 승인 대기 중인 판매자 목록을 조회합니다.
     * (isApproved == false AND approvedAt == null)
     */
    List<PendingSellerDTO> getPendingApprovalSellers();

    /**
     * 관리자가 판매자에 대해 승인 또는 거부 처리를 합니다.
     *
     * @param sellerId 처리할 판매자의 ID
     * @param approve  true이면 승인, false이면 거부
     * @param approvingAdminId 처리한 관리자의 ID (로깅용)
     * @return 처리 결과 (승인/거부된 판매자 정보 DTO)
     * @throws java.util.NoSuchElementException 해당 sellerId의 판매자가 없을 경우
     * @throws IllegalStateException 이미 처리된(approvedAt이 null이 아닌) 판매자를 다시 처리하려 할 경우 등
     */
    SellerResponseDTO processSellerDecision(Long sellerId, boolean approve, Integer approvingAdminId);
    // 반환 타입을 SellerResponseDTO로 할지, 아니면 PendingSellerDTO와 유사한 '처리 결과 DTO'를 새로 만들지 고민 필요.
    // 여기서는 SellerResponseDTO를 재활용하여 isApproved 상태를 명확히 보여주는 것으로 가정.

    // 승인된 전체 판매자 리스트
    List<SellerResponseDTO> getApprovedSellers();

}
