package com.lifelogix.timeline.category.api.dto.request;

public record CreateCategoryRequest(
        String name,
        String color,
        Long parentId
) {}