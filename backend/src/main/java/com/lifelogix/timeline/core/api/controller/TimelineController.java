package com.lifelogix.timeline.core.api.controller;

import com.lifelogix.timeline.core.api.dto.request.CreateTimeBlockRequest;
import com.lifelogix.timeline.core.api.dto.request.UpdateTimeBlockRequest;
import com.lifelogix.timeline.core.api.dto.response.BlockDetailResponse;
import com.lifelogix.timeline.core.api.dto.response.TimelineResponse;
import com.lifelogix.timeline.core.application.TimelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
     * 특정 시간 슬롯에 활동을 기록(생성 또는 수정)
     **/
    @PostMapping("/block")
    public ResponseEntity<BlockDetailResponse> createOrUpdateTimeBlock(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateTimeBlockRequest request) {

        BlockDetailResponse response = timelineService.createOrUpdateTimeBlock(userId, request);

        // 일관성을 위해 201 Created로 응답하고 Location 헤더는 비움
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 타임블록의 활동을 변경
     **/
    @PutMapping("/block/{timeBlockId}")
    public ResponseEntity<BlockDetailResponse> updateTimeBlock(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long timeBlockId,
            @Valid @RequestBody UpdateTimeBlockRequest request) {

        BlockDetailResponse response = timelineService.updateTimeBlock(userId, timeBlockId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 타임블록을 삭제
     **/
    @DeleteMapping("/block/{timeBlockId}")
    public ResponseEntity<Void> deleteTimeBlock(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long timeBlockId) {

        timelineService.deleteTimeBlock(userId, timeBlockId);
        return ResponseEntity.noContent().build();
    }
}