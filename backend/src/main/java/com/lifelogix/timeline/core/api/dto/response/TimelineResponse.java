package com.lifelogix.timeline.core.api.dto.response;

import java.time.LocalDate;
import java.util.List;

// 최종 응답의 최상위 DTO
public record TimelineResponse(
        LocalDate date,
        List<TimeBlockResponse> timeBlocks
) {}