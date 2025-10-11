package com.lifelogix.timeline.activity.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateActivityRequest(
        @NotBlank(message = "활동 이름은 필수입니다.")
        String name
) {}