package com.lifelogix.timeline.core.api.dto.response;

import com.lifelogix.timeline.activity.domain.Activity;
import com.lifelogix.timeline.core.domain.TimeBlock;

public record BlockDetailResponse(
        Long timeBlockId,
        Long activityId,
        String activityName,
        String categoryName,
        String categoryColor
) {
    public static BlockDetailResponse from(TimeBlock timeBlock) {
        Activity activity = timeBlock.getActivity();
        return new BlockDetailResponse(
                timeBlock.getId(),
                activity.getId(),
                activity.getName(),
                activity.getCategory().getName(),
                activity.getCategory().getColor()
        );
    }
}