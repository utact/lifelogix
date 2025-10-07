package com.lifelogix.timeline.activity.api.controller;

import com.lifelogix.timeline.activity.api.dto.request.CreateActivityRequest;
import com.lifelogix.timeline.activity.api.dto.response.ActivitiesByCategoryResponse;
import com.lifelogix.timeline.activity.api.dto.response.ActivityResponse;
import com.lifelogix.timeline.activity.application.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /**
     * 새로운 활동을 생성
     **/
    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateActivityRequest request) {

        ActivityResponse response = activityService.createActivity(userId, request);

        // 생성된 활동의 URI를 Location 헤더에 담아 201 Created 응답 반환
        URI location = URI.create("/api/v1/activities/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * 사용자가 정의한 모든 활동 목록을 카테고리별로 그룹화하여 조회
     **/
    @GetMapping
    public ResponseEntity<List<ActivitiesByCategoryResponse>> getAllActivities(
            @AuthenticationPrincipal Long userId) {

        List<ActivitiesByCategoryResponse> responses = activityService.findAllActivitiesGroupedByCategory(userId);
        return ResponseEntity.ok(responses);
    }
}