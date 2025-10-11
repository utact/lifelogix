package com.lifelogix.timeline.core.application;

import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.timeline.activity.domain.Activity;
import com.lifelogix.timeline.activity.domain.ActivityRepository;
import com.lifelogix.timeline.core.api.dto.request.CreateTimeBlockRequest;
import com.lifelogix.timeline.core.api.dto.request.UpdateTimeBlockRequest;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimelineService {

    private final TimeBlockRepository timeBlockRepository;
    private final ActivityRepository activityRepository;

    @Transactional
    public BlockDetailResponse createOrUpdateTimeBlock(Long userId, CreateTimeBlockRequest request) {
        Activity activity = findActivityById(request.activityId());
        validateActivityOwner(userId, activity);

        // 동일한 시간대에 이미 TimeBlock이 존재하는지 확인
        Optional<TimeBlock> existingBlockOpt = timeBlockRepository.findUserTimeBlockForSlot(
                userId, request.date(), request.startTime(), request.type());

        TimeBlock timeBlock;
        if (existingBlockOpt.isPresent()) {
            // 존재하면, 활동(Activity)만 업데이트
            timeBlock = existingBlockOpt.get();
            timeBlock.updateActivity(activity);
        } else {
            // 존재하지 않으면, 새로 생성
            timeBlock = new TimeBlock(
                    request.date(),
                    request.startTime(),
                    request.type(),
                    activity
            );
            timeBlockRepository.save(timeBlock);
        }

        return BlockDetailResponse.from(timeBlock);
    }

    public TimelineResponse getDailyTimeline(Long userId, LocalDate date) {
        List<TimeBlock> timeBlocks = timeBlockRepository.findByUserIdAndDate(userId, date);
        Map<LocalTime, List<TimeBlock>> blocksByTime = timeBlocks.stream()
                .collect(Collectors.groupingBy(TimeBlock::getStartTime));

        List<TimeBlockResponse> timeBlockResponses = blocksByTime.entrySet().stream()
                .map(entry -> {
                    BlockDetailResponse plan = findAndMapToDetail(entry.getValue(), TimeBlockType.PLAN);
                    BlockDetailResponse actual = findAndMapToDetail(entry.getValue(), TimeBlockType.ACTUAL);
                    return new TimeBlockResponse(entry.getKey(), plan, actual);
                })
                .sorted(Comparator.comparing(TimeBlockResponse::startTime))
                .toList();

        return new TimelineResponse(date, timeBlockResponses);
    }

    @Transactional
    public BlockDetailResponse updateTimeBlock(Long userId, Long timeBlockId, UpdateTimeBlockRequest request) {
        TimeBlock timeBlock = findTimeBlockById(timeBlockId);
        validateTimeBlockOwner(userId, timeBlock);

        Activity newActivity = findActivityById(request.activityId());
        validateActivityOwner(userId, newActivity);

        timeBlock.updateActivity(newActivity);
        return BlockDetailResponse.from(timeBlock);
    }

    @Transactional
    public void deleteTimeBlock(Long userId, Long timeBlockId) {
        TimeBlock timeBlock = findTimeBlockById(timeBlockId);
        validateTimeBlockOwner(userId, timeBlock);

        timeBlockRepository.delete(timeBlock);
    }

    private BlockDetailResponse findAndMapToDetail(List<TimeBlock> blocks, TimeBlockType type) {
        return blocks.stream()
                .filter(block -> block.getType() == type)
                .findFirst()
                .map(BlockDetailResponse::from)
                .orElse(null);
    }

    // --- Private Helper Methods ---

    private Activity findActivityById(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));
    }

    private TimeBlock findTimeBlockById(Long timeBlockId) {
        return timeBlockRepository.findById(timeBlockId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_BLOCK_NOT_FOUND));
    }

    private void validateActivityOwner(Long userId, Activity activity) {
        if (!activity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private void validateTimeBlockOwner(Long userId, TimeBlock timeBlock) {
        if (!timeBlock.getActivity().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
    }
}