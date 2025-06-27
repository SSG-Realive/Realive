package com.realive.dto.admin.approval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveSellerResponseDTO {
    
    private boolean success;

    private String message;

    private LocalDateTime timestamp;
}
