package com.lifelogix.timeline.core.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateTimeBlockRequest(
        @NotNull(message = "활동 ID는 필수입니다.")
        Long activityId
) {}