package com.lifelogix.timeline.core.api.controller;

import com.lifelogix.timeline.core.api.dto.request.CreateTimeBlockRequest;
import com.lifelogix.timeline.core.api.dto.response.BlockDetailResponse;
import com.lifelogix.timeline.core.api.dto.response.TimelineResponse;
import com.lifelogix.timeline.core.application.TimelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/timeline")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    /**
     * 특정 날짜의 타임라인을 조회
     **/
    @GetMapping
    public ResponseEntity<TimelineResponse> getDailyTimeline(
            @AuthenticationPrincipal Long userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        TimelineResponse response = timelineService.getDailyTimeline(userId, date);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 시간 슬롯에 활동을 기록(생성/수정)
     **/
    @PostMapping("/block")
    public ResponseEntity<BlockDetailResponse> createTimeBlock(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateTimeBlockRequest request) {

        BlockDetailResponse response = timelineService.createTimeBlock(userId, request);

        // 생성된 리소스에 대한 URI를 만들어 반환하는 것이 RESTful API의 좋은 관례
        // 그러나 -> TimeBlock은 단일 리소스로 조회하는 API가 없으므로 body만 반환
        return ResponseEntity.created(URI.create("")).body(response);
    }
}