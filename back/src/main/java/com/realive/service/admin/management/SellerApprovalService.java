package com.realive.service.admin.management;

import com.realive.dto.admin.management.ApproveSellerRequestDTO;
import com.realive.dto.admin.management.ApproveSellerResponseDTO;

public interface SellerApprovalService {
    ApproveSellerResponseDTO processSellerApproval(ApproveSellerRequestDTO requestDto, Integer approvingAdminId);
}
