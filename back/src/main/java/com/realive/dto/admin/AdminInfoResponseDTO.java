package com.realive.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 관리자 정보 조회
@Getter
@AllArgsConstructor
public class AdminInfoResponseDTO {
    private String name;
    private String email;
    private String role;
}
