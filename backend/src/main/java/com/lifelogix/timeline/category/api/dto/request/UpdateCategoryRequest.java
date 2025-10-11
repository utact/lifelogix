package com.lifelogix.timeline.category.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateCategoryRequest(
        @NotBlank String name,
        @NotBlank String color
) {}