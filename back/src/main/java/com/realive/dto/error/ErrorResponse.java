package com.realive.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ErrorResponse DTO
 * API 호출 시 발생하는 예외 정보를 클라이언트에게 전달하기 위한 응답 포맷 클래스
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /** HTTP 상태 코드 (예: 400, 404, 500 등) */
    private int status;

    /** 에러 코드 (내부 정의된 비즈니스용 에러 코드 등) */
    private String code;

    /** 에러 메시지 (예: "Invalid request", "User not found" 등) */
    private String message;
}