package com.realive.exception;

// 커스텀 예외처리: 이메일 중복

public class DuplicateEmailException extends RuntimeException {
    
    public DuplicateEmailException(String message) {
        super(message);
    }
}
