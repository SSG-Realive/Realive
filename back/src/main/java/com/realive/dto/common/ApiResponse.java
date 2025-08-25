// src/main/java/com/realive/dto/common/ApiResponse.java
package com.realive.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Jackson 및 상속을 위한 기본 생성자 (외부 직접 사용 제한)
public class ApiResponse<T> {

    private int status;       // HTTP 상태 코드와 일치하거나, 내부적으로 정의한 상태 코드
    private String message;   // 응답 메시지 (성공/실패 메시지)
    private T data;           // 실제 응답 데이터
    private LocalDateTime timestamp; // 응답 시간

    // 모든 필드를 초기화하는 private 생성자 (정적 팩토리 메소드에서 사용)
    private ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 정적 팩토리 메소드 (성공, 데이터 포함)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data); // HTTP 200 OK
    }

    // 정적 팩토리 메소드 (성공, 메시지 및 데이터 포함)
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data); // HTTP 200 OK
    }

    // 정적 팩토리 메소드 (실패)
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null); // 실패 시 data는 null
    }

    // (선택 사항) 다른 HTTP 상태 코드에 대한 정적 팩토리 메소드 추가 가능
    // 예: public static <T> ApiResponse<T> created(T data) { return new ApiResponse<>(201, "Created", data); }
}
