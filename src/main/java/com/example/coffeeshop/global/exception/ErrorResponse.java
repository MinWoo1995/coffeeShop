package com.example.coffeeshop.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final String code;
    private final String message;
    private final LocalDateTime timestamp;

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                LocalDateTime.now()
        );
    }
}