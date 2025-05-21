package com.realive.dto.admin.management;

import com.realive.domain.common.enums.SellerApprovalStatusByAdmin;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveSellerRequestDTO {
    
    @NotNull
    private Integer sellerId;

    @NotNull
    private SellerApprovalStatusByAdmin status;

    @Nullable
    private String reason;
}
