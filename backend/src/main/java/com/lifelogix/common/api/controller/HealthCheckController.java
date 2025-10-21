package com.lifelogix.common.api.controller;

import com.lifelogix.common.api.dto.response.HealthCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthCheckController {

    private final BuildProperties buildProperties;

    /**
     * 애플리케이션의 상태, 버전을 반환하여 헬스 체크를 수행 (커밋 해시 제외)
     * @return HealthCheckResponse DTO를 포함하는 ResponseEntity
     */
    @GetMapping("/api/v1/health")
    public ResponseEntity<HealthCheckResponse> healthCheck() {
        // GitProperties 대신 빈 문자열("") 사용
        HealthCheckResponse response = new HealthCheckResponse(
                "UP",
                buildProperties.getVersion(),
                "" // gitProperties.getShortCommitId() 대신 빈 문자열
        );
        return ResponseEntity.ok(response);
    }
}