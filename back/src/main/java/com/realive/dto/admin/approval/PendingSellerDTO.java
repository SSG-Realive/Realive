package com.realive.dto.admin.approval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder // 서비스에서 빌더 패턴으로 생성하기 위해 추가
@NoArgsConstructor
@AllArgsConstructor
public class PendingSellerDTO {
    private Long id;

    private String name;

    private String email;

    private String businessNumber;

}
