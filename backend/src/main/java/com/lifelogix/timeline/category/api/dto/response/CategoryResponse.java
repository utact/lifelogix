package com.lifelogix.timeline.category.api.dto.response;

import com.lifelogix.timeline.category.domain.Category;

public record CategoryResponse(
        Long id,
        String name,
        String color,
        boolean isCustom,
        Long parentId
) {
    public static CategoryResponse from(Category category) {
        Long parentId = (category.getParent() != null) ? category.getParent().getId() : null;
        boolean isCustom = (category.getUser() != null);

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor(),
                isCustom,
                parentId
        );
    }
}