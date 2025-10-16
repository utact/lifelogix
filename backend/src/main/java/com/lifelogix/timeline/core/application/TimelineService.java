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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(TimelineService.class);
    private final TimeBlockRepository timeBlockRepository;
    private final ActivityRepository activityRepository;

    @Transactional
    public BlockDetailResponse createOrUpdateTimeBlock(Long userId, CreateTimeBlockRequest request) {
        log.info("[Backend|TimelineService] CreateOrUpdate - Attempt for userId: {} at {} on {}", userId, request.startTime(), request.date());
        Activity activity = findActivityById(request.activityId());
        validateActivityOwner(userId, activity);

        Optional<TimeBlock> existingBlockOpt = timeBlockRepository.findUserTimeBlockForSlot(
                userId, request.date(), request.startTime(), request.type());

        TimeBlock timeBlock;
        if (existingBlockOpt.isPresent()) {
            timeBlock = existingBlockOpt.get();
            timeBlock.updateActivity(activity);
            log.info("[Backend|TimelineService] CreateOrUpdate - Updated existing TimeBlockId: {}", timeBlock.getId());
        } else {
            timeBlock = new TimeBlock(
                    request.date(),
                    request.startTime(),
                    request.type(),
                    activity
            );
            timeBlockRepository.save(timeBlock);
            log.info("[Backend|TimelineService] CreateOrUpdate - Created new TimeBlockId: {}", timeBlock.getId());
        }

        return BlockDetailResponse.from(timeBlock);
    }

    public TimelineResponse getDailyTimeline(Long userId, LocalDate date) {
        log.info("[Backend|TimelineService] GetDailyTimeline - Attempt for userId: {} on date: {}", userId, date);
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

        log.info("[Backend|TimelineService] GetDailyTimeline - Success for userId: {}. Found {} time blocks.", userId, timeBlockResponses.size());
        return new TimelineResponse(date, timeBlockResponses);
    }

    @Transactional
    public BlockDetailResponse updateTimeBlock(Long userId, Long timeBlockId, UpdateTimeBlockRequest request) {
        log.info("[Backend|TimelineService] UpdateTimeBlock - Attempt for userId: {} on timeBlockId: {}", userId, timeBlockId);
        TimeBlock timeBlock = findTimeBlockById(timeBlockId);
        validateTimeBlockOwner(userId, timeBlock);

        Activity newActivity = findActivityById(request.activityId());
        validateActivityOwner(userId, newActivity);

        timeBlock.updateActivity(newActivity);
        log.info("[Backend|TimelineService] UpdateTimeBlock - Success for userId: {} on timeBlockId: {}", userId, timeBlockId);
        return BlockDetailResponse.from(timeBlock);
    }

    @Transactional
    public void deleteTimeBlock(Long userId, Long timeBlockId) {
        log.info("[Backend|TimelineService] DeleteTimeBlock - Attempt for userId: {} on timeBlockId: {}", userId, timeBlockId);
        TimeBlock timeBlock = findTimeBlockById(timeBlockId);
        validateTimeBlockOwner(userId, timeBlock);

        timeBlockRepository.delete(timeBlock);
        log.info("[Backend|TimelineService] DeleteTimeBlock - Success for userId: {} on timeBlockId: {}", userId, timeBlockId);
    }

    private BlockDetailResponse findAndMapToDetail(List<TimeBlock> blocks, TimeBlockType type) {
        return blocks.stream()
                .filter(block -> block.getType() == type)
                .findFirst()
                .map(BlockDetailResponse::from)
                .orElse(null);
    }

    private Activity findActivityById(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> {
                    log.warn("[Backend|TimelineService] FindActivityById - Failed: Activity not found for id: {}", activityId);
                    return new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
                });
    }

    private TimeBlock findTimeBlockById(Long timeBlockId) {
        return timeBlockRepository.findById(timeBlockId)
                .orElseThrow(() -> {
                    log.warn("[Backend|TimelineService] FindTimeBlockById - Failed: TimeBlock not found for id: {}", timeBlockId);
                    return new BusinessException(ErrorCode.TIME_BLOCK_NOT_FOUND);
                });
    }

    private void validateActivityOwner(Long userId, Activity activity) {
        if (!activity.getUser().getId().equals(userId)) {
            log.warn("[Backend|TimelineService] ValidateActivityOwner - Failed: Permission denied for userId: {} on activityId: {}", userId, activity.getId());
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private void validateTimeBlockOwner(Long userId, TimeBlock timeBlock) {
        if (!timeBlock.getActivity().getUser().getId().equals(userId)) {
            log.warn("[Backend|TimelineService] ValidateTimeBlockOwner - Failed: Permission denied for userId: {} on timeBlockId: {}", userId, timeBlock.getId());
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
    }
}