package com.lifelogix.common.api.dto.response;

/**
 * Health Check API의 응답 상태를 담는 DTO
 * @param status 서버의 현재 상태 (e.g., "UP")
 * @param version 애플리케이션의 현재 버전
 * @param commitHash 마지막 빌드의 Git 커밋 해시
 */
public record HealthCheckResponse(String status, String version, String commitHash) {
}
