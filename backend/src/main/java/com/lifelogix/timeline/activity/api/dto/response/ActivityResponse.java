package com.lifelogix.timeline.activity.api.dto.response;

import com.lifelogix.timeline.activity.domain.Activity;

public record ActivityResponse(
        Long id,
        String name
) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(activity.getId(), activity.getName());
    }
}