package com.realive.dto.admin.management;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveSellerResponseDTO {
    
    private boolean success;

    private String message;

    private LocalDateTime timestamp;
}
