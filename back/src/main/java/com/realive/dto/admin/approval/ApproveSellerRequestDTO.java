package com.realive.dto.admin.approval;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApproveSellerRequestDTO {
    @NotNull
    private Long sellerId;
    @NotNull
    private boolean approve;
}
