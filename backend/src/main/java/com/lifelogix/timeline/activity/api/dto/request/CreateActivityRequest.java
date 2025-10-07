package com.lifelogix.timeline.activity.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateActivityRequest(
        @NotBlank String name,
        @NotNull Long categoryId
) {}