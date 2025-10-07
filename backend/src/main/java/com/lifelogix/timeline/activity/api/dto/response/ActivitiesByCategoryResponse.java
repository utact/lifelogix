package com.lifelogix.timeline.activity.api.dto.response;

import java.util.List;

public record ActivitiesByCategoryResponse(
        Long categoryId,
        String categoryName,
        List<ActivityResponse> activities
) {}