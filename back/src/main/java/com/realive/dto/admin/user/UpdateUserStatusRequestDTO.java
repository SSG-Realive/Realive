package com.realive.dto.admin.user;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // JSON 역직렬화를 위해 기본 생성자 필요
public class UpdateUserStatusRequestDTO {
    private Boolean isActive; // 변경할 활성 상태 (true 또는 false)
    // 필요하다면 상태 변경 사유 등의 필드 추가 가능
}
