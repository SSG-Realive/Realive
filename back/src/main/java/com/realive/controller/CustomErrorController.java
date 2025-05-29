package com.realive.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realive.dto.error.ErrorResponse;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

// 예외 핸들링
// 흐름정리 - 잘못된 경로 입력 시 500 상태코드 보내는 문제 해결 

@RestController
@RequestMapping("/error")
public class CustomErrorController implements ErrorController {

    @RequestMapping
    public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;

        if (statusCode == 404) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse(404, "NOT_FOUND", "요청하신 경로가 존재하지 않습니다."));
        } else if (statusCode == 403) {
            return ResponseEntity.status(403)
                .body(new ErrorResponse(403, "FORBIDDEN", "접근이 거부되었습니다."));
        } else if (statusCode == 405) {
            return ResponseEntity.status(405)
                .body(new ErrorResponse(405, "METHOD_NOT_ALLOWED", "허용되지 않은 HTTP 메서드입니다."));
        }

        return ResponseEntity.status(500)
                .body(new ErrorResponse(500, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."));
    }
}
