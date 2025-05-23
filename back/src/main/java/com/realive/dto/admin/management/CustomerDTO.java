package com.realive.dto.admin.management;

import jdk.jshell.Snippet;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
// 고객 관리
public class CustomerDTO {
    private Integer id;
    private String name;
    private String email;
    private String status;
    private LocalDateTime registeredAt;
    private Integer orderCount;
    private Integer totalSpent;


}
