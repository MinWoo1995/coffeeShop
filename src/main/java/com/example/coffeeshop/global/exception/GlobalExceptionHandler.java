package com.example.coffeeshop.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("[CustomException] code: {}, message: {}",
                e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[Exception] {}", e.getMessage(), e);
        return ResponseEntity
                .status(500)
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
