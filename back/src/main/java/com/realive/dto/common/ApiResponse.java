package com.realive.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude; // Null 값 필드 제외를 위해 (선택 사항)
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // 응답 시 data 필드가 null이면 JSON에서 제외 (선택 사항)
public class ApiResponse<T> {
    private final int status;       // HTTP 상태 코드와 일치하거나, 내부적으로 정의한 상태 코드
    private final String message;   // 응답 메시지 (성공/실패 메시지)
    private final T data;           // 실제 응답 데이터
    private final LocalDateTime timestamp; // 응답 시간

    // 성공 시 사용하는 생성자 (데이터 포함)
    private ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 성공 시 사용하는 생성자 (데이터만 포함, 메시지는 기본 "Success")
    private ApiResponse(int status, T data) {
        this.status = status;
        this.message = "Success"; // 기본 성공 메시지
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 실패 시 사용하는 생성자 (데이터는 null)
    private ApiResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.data = null; // 실패 시 데이터는 null
        this.timestamp = LocalDateTime.now();
    }

    // 정적 팩토리 메소드 (성공, 데이터 포함)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, data); // HTTP 200 OK
    }

    // 정적 팩토리 메소드 (성공, 메시지 및 데이터 포함)
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data); // HTTP 200 OK
    }

    // 정적 팩토리 메소드 (실패)
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message);
    }

    // (선택 사항) 다른 HTTP 상태 코드에 대한 정적 팩토리 메소드 추가 가능
    // 예: public static <T> ApiResponse<T> created(T data) { return new ApiResponse<>(201, "Created", data); }
}
