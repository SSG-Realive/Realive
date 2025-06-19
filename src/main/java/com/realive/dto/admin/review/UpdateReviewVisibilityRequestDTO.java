package com.realive.dto.admin.review;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // JSON 역직렬화를 위해 기본 생성자 추가
public class UpdateReviewVisibilityRequestDTO {

    @NotNull(message = "숨김 상태(isHidden) 값은 필수입니다.")
    private Boolean isHidden;
}
