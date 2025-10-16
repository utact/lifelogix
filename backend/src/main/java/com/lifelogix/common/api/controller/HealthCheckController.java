package com.lifelogix.common.api.controller;

import com.lifelogix.common.api.dto.response.HealthCheckResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    /**
     * 애플리케이션의 상태, 버전, 커밋 해시를 반환하여 헬스 체크를 수행
     * @return HealthCheckResponse DTO를 포함하는 ResponseEntity
     **/
    @GetMapping("/api/v1/health")
    public ResponseEntity<HealthCheckResponse> healthCheck() {
        // TODO: 빌드 시 주입되는 프로퍼티 값을 읽어오도록 구성
        HealthCheckResponse response = new HealthCheckResponse("UP", "1.0.0-logging", "f1f1900");
        return ResponseEntity.ok(response);
    }
}