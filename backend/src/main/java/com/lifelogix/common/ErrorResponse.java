package com.lifelogix.common;

import java.time.LocalDateTime;

/**
 * API 에러 발생 시 클라이언트에게 반환될 표준 응답 DTO
 * @param timestamp 에러 발생 시간
 * @param status HTTP 상태 코드
 * @param error HTTP 상태 메시지 (e.g., "Bad Request")
 * @param message 에러에 대한 상세 메시지
 * @param path 요청이 발생한 URI
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
