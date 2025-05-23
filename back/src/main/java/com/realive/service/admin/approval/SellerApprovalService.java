package com.realive.service.admin.approval;

import com.realive.dto.admin.approval.ApproveSellerResponseDTO;

public interface SellerApprovalService {

    /**
     * 관리자가 판매자의 승인 상태를 처리합니다. (승인, 거절, 보류 등)
     *
     * @param requestDto 판매자 승인 요청 정보를 담은 DTO (sellerId, newStatus, reason 등 포함)
     * @param approvingAdminId 승인/거절을 처리하는 관리자의 ID (추적 및 로깅용)
     * @return 처리 결과 및 판매자의 새로운 상태 정보를 담은 DTO
     */
    ApproveSellerResponseDTO processSellerApproval(ApproveSellerRequestDTO requestDto, Integer approvingAdminId);

    // 필요에 따라 판매자 승인과 관련된 다른 관리 기능 메소드를 추가할 수 있습니다.
    // 예: 특정 판매자의 현재 승인 상태 조회, 승인 이력 조회 등
}
