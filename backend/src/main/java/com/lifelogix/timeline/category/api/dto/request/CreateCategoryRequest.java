package com.lifelogix.timeline.category.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCategoryRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        String name,

        @NotBlank(message = "색상은 필수입니다.")
        String color,

        @NotNull(message = "부모 카테고리 ID는 필수입니다.")
        Long parentId
) {}