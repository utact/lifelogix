package com.lifelogix.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 BAD_REQUEST
    INVALID_PARENT_CATEGORY(HttpStatus.BAD_REQUEST, "시스템 기본 카테고리만 부모로 지정할 수 있습니다."),
    CATEGORY_IN_USE(HttpStatus.BAD_REQUEST, "해당 카테고리를 사용하는 활동이 존재하여 삭제할 수 없습니다."),
    ACTIVITY_IN_USE(HttpStatus.BAD_REQUEST, "해당 활동을 사용하는 타임블록이 존재하여 삭제할 수 없습니다."),

    // 401 UNAUTHORIZED
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    // 403 FORBIDDEN
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "해당 리소스를 사용할 권한이 없습니다."),

    // 404 NOT_FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "활동을 찾을 수 없습니다."),
    TIME_BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "타임블록을 찾을 수 없습니다."),

    // 409 CONFLICT
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    CATEGORY_NAME_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 카테고리 이름입니다."),
    ACTIVITY_NAME_DUPLICATE(HttpStatus.CONFLICT, "해당 카테고리에 이미 사용 중인 활동 이름입니다.");

    private final HttpStatus status;
    private final String message;
}