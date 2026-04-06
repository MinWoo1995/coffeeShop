package com.example.coffeeshop.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 사용자입니다."),
    INVALID_CHARGE_AMOUNT(HttpStatus.BAD_REQUEST, "U002", "충전 금액은 0보다 커야 합니다."),
    INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "U003", "포인트 잔액이 부족합니다."),

    // Menu
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 메뉴입니다."),
    MENU_UNAVAILABLE(HttpStatus.CONFLICT, "M002", "판매 중단된 메뉴입니다."),

    // Server
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}