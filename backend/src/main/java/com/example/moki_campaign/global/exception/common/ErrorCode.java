package com.example.moki_campaign.global.exception.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "JSON_PARSE_ERROR", "요청 본문을 해석할 수 없습니다."),

    // Domain Common
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", "엔티티를 찾을 수 없습니다."),
    DUPLICATE_KEY(HttpStatus.CONFLICT, "DUPLICATE_KEY", "이미 존재하는 값입니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증에 실패했습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "사업자번호 또는 비밀번호가 올바르지 않습니다."),
    EXTERNAL_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "EXTERNAL_AUTH_FAILED", "외부 인증 서버 연결에 실패했습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "만료된 토큰입니다."),

    // Store
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE_NOT_FOUND", "매장 데이터를 찾을 수 없습니다."),

    // Customer
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "고객 데이터를 찾을 수 없습니다."),

    // Visit

    // AI
    INVAILD_AI_SERVER_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "INVAILD_AI_SERVER_RESPONSE", "AI 분석 서버로부터 유효한 응답을 받지 못했습니다."),
    AI_SERVER_CONNECT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI_SERVER_CONNECT_ERROR", "AI 분석 서버 호출에 실패했습니다.");


    public final HttpStatus status;
    public final String code;
    public final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}