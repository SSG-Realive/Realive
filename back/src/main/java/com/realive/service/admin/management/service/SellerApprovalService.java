package com.realive.service.admin.management.service;

import com.realive.dto.admin.approval.ApproveSellerRequestDTO;
import com.realive.dto.admin.approval.ApproveSellerResponseDTO;

public interface SellerApprovalService {
    ApproveSellerResponseDTO processSellerApproval(ApproveSellerRequestDTO requestDto, Integer approvingAdminId);
}
