package com.lifelogix.timeline.core.api.dto.response;

import com.lifelogix.timeline.activity.domain.Activity;

// `plan` 또는 `actual`에 들어갈 상세 활동 정보를 담는 DTO
public record BlockDetailResponse(
        Long activityId,
        String activityName,
        String categoryName,
        String categoryColor
) {
    public static BlockDetailResponse from(Activity activity) {
        return new BlockDetailResponse(
                activity.getId(),
                activity.getName(),
                activity.getCategory().getName(),
                activity.getCategory().getColor()
        );
    }
}