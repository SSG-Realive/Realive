package com.realive.dto.admin.approval;

import com.realive.domain.common.enums.SellerApprovalStatusByAdmin;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveSellerRequestDTO {
    
    @NotNull
    private Integer sellerId;

    @NotNull
    private SellerApprovalStatusByAdmin status;

}
