package com.realive.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.realive.dto.error.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {


    //인풋 에러 처리하는 코드
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e){
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("INVALID_INPUT")
                .message(e.getMessage())
                .build());
    }


    //서버 에러 처리하는 코드
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e){
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code("INTERNAL_SERVER_ERROR")
                .message("서버 오류가 발생했습니다")
                .build());
    }
            
                
    
}
