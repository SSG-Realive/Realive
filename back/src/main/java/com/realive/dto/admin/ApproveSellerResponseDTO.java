package com.realive.dto.admin;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveSellerResponseDTO {
    
    private boolean success;

    private String message;

    private LocalDateTime timestamp;
}
