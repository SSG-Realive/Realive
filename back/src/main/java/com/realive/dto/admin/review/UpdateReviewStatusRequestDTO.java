package com.realive.dto.admin.review;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateReviewStatusRequestDTO { //리뷰 상태 변경 요청용
    private Boolean isHidden; // 변경할 숨김/공개 상태
    private String reasonForChange; // 상태 변경 사유 (관리자용)
}
