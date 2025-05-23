package com.realive.dto.admin.management;

import jdk.jshell.Snippet;

import java.time.LocalDateTime;

// 고객 관리
public class CustomerDTO {
    private Integer id;
    private String name;
    private String email;
    private String status;
    private LocalDateTime registeredAt;
    private Integer orderCount;
    private Integer totalSpent;

    public static Snippet builder() {
    }
    // 기타 필요한 고객 정보
}
