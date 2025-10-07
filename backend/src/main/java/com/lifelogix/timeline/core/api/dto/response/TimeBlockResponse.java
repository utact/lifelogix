package com.lifelogix.timeline.core.api.dto.response;

import java.time.LocalTime;

// 30분 단위의 각 블록을 표현하는 DTO
public record TimeBlockResponse(
        LocalTime startTime,
        BlockDetailResponse plan,
        BlockDetailResponse actual
) {}