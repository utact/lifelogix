package com.lifelogix.timeline.activity.api.controller;

import com.lifelogix.timeline.activity.api.dto.request.CreateActivityRequest;
import com.lifelogix.timeline.activity.api.dto.request.UpdateActivityRequest;
import com.lifelogix.timeline.activity.api.dto.response.ActivitiesByCategoryResponse;
import com.lifelogix.timeline.activity.api.dto.response.ActivityResponse;
import com.lifelogix.timeline.activity.application.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
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
            Principal principal,
            @Valid @RequestBody CreateActivityRequest request) {

        Long userId = Long.parseLong(principal.getName());
        ActivityResponse response = activityService.createActivity(userId, request);
        URI location = URI.create("/api/v1/activities/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * 사용자가 정의한 모든 활동 목록을 카테고리별로 그룹화하여 조회
     **/
    @GetMapping
    public ResponseEntity<List<ActivitiesByCategoryResponse>> getAllActivities(
            Principal principal) {

        Long userId = Long.parseLong(principal.getName());
        List<ActivitiesByCategoryResponse> responses = activityService.findAllActivitiesGroupedByCategory(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 자신의 활동 정보를 수정
     **/
    @PutMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> updateActivity(
            Principal principal,
            @PathVariable Long activityId,
            @Valid @RequestBody UpdateActivityRequest request) {

        Long userId = Long.parseLong(principal.getName());
        ActivityResponse response = activityService.updateActivity(userId, activityId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 자신의 활동을 삭제
     **/
    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(
            Principal principal,
            @PathVariable Long activityId) {

        Long userId = Long.parseLong(principal.getName());
        activityService.deleteActivity(userId, activityId);
        return ResponseEntity.noContent().build();
    }
}