package com.lifelogix.timeline.core.api.dto.request;

import com.lifelogix.timeline.core.domain.TimeBlockType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateTimeBlockRequest(
        @NotNull LocalDate date,
        @NotNull LocalTime startTime,
        @NotNull TimeBlockType type,
        @NotNull Long activityId
) {}