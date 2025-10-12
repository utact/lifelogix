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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
            Principal principal,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long userId = Long.parseLong(principal.getName());
        TimelineResponse response = timelineService.getDailyTimeline(userId, date);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 시간 슬롯에 활동을 기록(생성 또는 수정)
     **/
    @PostMapping("/block")
    public ResponseEntity<BlockDetailResponse> createOrUpdateTimeBlock(
            Principal principal,
            @Valid @RequestBody CreateTimeBlockRequest request) {

        Long userId = Long.parseLong(principal.getName());
        BlockDetailResponse response = timelineService.createOrUpdateTimeBlock(userId, request);

        // 일관성을 위해 201 Created로 응답하고 Location 헤더는 비움
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 타임블록의 활동을 변경
     **/
    @PutMapping("/block/{timeBlockId}")
    public ResponseEntity<BlockDetailResponse> updateTimeBlock(
            Principal principal,
            @PathVariable Long timeBlockId,
            @Valid @RequestBody UpdateTimeBlockRequest request) {

        Long userId = Long.parseLong(principal.getName());
        BlockDetailResponse response = timelineService.updateTimeBlock(userId, timeBlockId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 타임블록을 삭제
     **/
    @DeleteMapping("/block/{timeBlockId}")
    public ResponseEntity<Void> deleteTimeBlock(
            Principal principal,
            @PathVariable Long timeBlockId) {

        Long userId = Long.parseLong(principal.getName());
        timelineService.deleteTimeBlock(userId, timeBlockId);
        return ResponseEntity.noContent().build();
    }
}