package com.lifelogix.timeline.core.application;

import com.lifelogix.timeline.activity.domain.Activity;
import com.lifelogix.timeline.activity.domain.ActivityRepository;
import com.lifelogix.timeline.core.api.dto.request.CreateTimeBlockRequest;
import com.lifelogix.timeline.core.api.dto.response.BlockDetailResponse;
import com.lifelogix.timeline.core.api.dto.response.TimeBlockResponse;
import com.lifelogix.timeline.core.api.dto.response.TimelineResponse;
import com.lifelogix.timeline.core.domain.TimeBlock;
import com.lifelogix.timeline.core.domain.TimeBlockRepository;
import com.lifelogix.timeline.core.domain.TimeBlockType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimelineService {

    private final TimeBlockRepository timeBlockRepository;
    private final ActivityRepository activityRepository;

    @Transactional
    public BlockDetailResponse createTimeBlock(Long userId, CreateTimeBlockRequest request) {
        Activity activity = activityRepository.findById(request.activityId())
                .orElseThrow(() -> new IllegalArgumentException("활동을 찾을 수 없습니다."));

        // 활동의 소유자가 현재 사용자인지 검증
        if (!activity.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 활동을 사용할 권한이 없습니다.");
        }

        TimeBlock newTimeBlock = new TimeBlock(
                request.date(),
                request.startTime(),
                request.type(),
                activity
        );
        timeBlockRepository.save(newTimeBlock);

        return BlockDetailResponse.from(activity);
    }

    public TimelineResponse getDailyTimeline(Long userId, LocalDate date) {
        // 1. 특정 사용자의 특정 날짜 TimeBlock 목록을 모두 조회
        List<TimeBlock> timeBlocks = timeBlockRepository.findByUserIdAndDate(userId, date);

        // 2. 조회된 TimeBlock들을 startTime을 기준으로 그룹핑
        Map<LocalTime, List<TimeBlock>> blocksByTime = timeBlocks.stream()
                .collect(Collectors.groupingBy(TimeBlock::getStartTime));

        // 3. 그룹핑된 데이터를 최종 응답 DTO 형태로 가공
        List<TimeBlockResponse> timeBlockResponses = new ArrayList<>();
        blocksByTime.forEach((startTime, blocks) -> {
            BlockDetailResponse plan = findAndMapToDetail(blocks, TimeBlockType.PLAN);
            BlockDetailResponse actual = findAndMapToDetail(blocks, TimeBlockType.ACTUAL);
            timeBlockResponses.add(new TimeBlockResponse(startTime, plan, actual));
        });

        // 4. 시간순으로 정렬
        timeBlockResponses.sort(Comparator.comparing(TimeBlockResponse::startTime));

        return new TimelineResponse(date, timeBlockResponses);
    }

    // List<TimeBlock>에서 특정 타입의 블록을 찾아 DTO로 변환하는 헬퍼 메서드
    private BlockDetailResponse findAndMapToDetail(List<TimeBlock> blocks, TimeBlockType type) {
        return blocks.stream()
                .filter(block -> block.getType() == type)
                .findFirst()
                .map(block -> BlockDetailResponse.from(block.getActivity()))
                .orElse(null);
    }
}