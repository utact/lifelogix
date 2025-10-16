package com.lifelogix.timeline.activity.application;

import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.timeline.activity.api.dto.request.CreateActivityRequest;
import com.lifelogix.timeline.activity.api.dto.request.UpdateActivityRequest;
import com.lifelogix.timeline.activity.api.dto.response.ActivitiesByCategoryResponse;
import com.lifelogix.timeline.activity.api.dto.response.ActivityResponse;
import com.lifelogix.timeline.activity.domain.Activity;
import com.lifelogix.timeline.activity.domain.ActivityRepository;
import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.timeline.category.domain.CategoryRepository;
import com.lifelogix.timeline.core.domain.TimeBlockRepository;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TimeBlockRepository timeBlockRepository;

    @Transactional
    public ActivityResponse createActivity(Long userId, CreateActivityRequest request) {
        log.info("[Backend|ActivityService] CreateActivity - Attempt for userId: {} with activityName: {}", userId, request.name());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            log.warn("[Backend|ActivityService] CreateActivity - Failed: Permission denied for userId: {} on categoryId: {}", userId, request.categoryId());
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        if (activityRepository.existsByCategoryAndName(category, request.name())) {
            log.warn("[Backend|ActivityService] CreateActivity - Failed: Duplicate activity name '{}' for userId: {}", request.name(), userId);
            throw new BusinessException(ErrorCode.ACTIVITY_NAME_DUPLICATE);
        }

        Activity newActivity = new Activity(request.name(), user, category);
        Activity savedActivity = activityRepository.save(newActivity);

        log.info("[Backend|ActivityService] CreateActivity - Success for userId: {} with new activityId: {}", userId, savedActivity.getId());
        return ActivityResponse.from(savedActivity);
    }

    public List<ActivitiesByCategoryResponse> findAllActivitiesGroupedByCategory(Long userId) {
        log.info("[Backend|ActivityService] FindAllActivities - Attempt for userId: {}", userId);
        List<Activity> activities = activityRepository.findByUserIdOrderByCategory(userId);

        Map<Category, List<Activity>> groupedActivities = activities.stream()
                .collect(Collectors.groupingBy(Activity::getCategory));

        List<ActivitiesByCategoryResponse> response = groupedActivities.entrySet().stream()
                .map(entry -> {
                    Category category = entry.getKey();
                    List<ActivityResponse> activityResponses = entry.getValue().stream()
                            .map(ActivityResponse::from)
                            .toList();
                    return new ActivitiesByCategoryResponse(category.getId(), category.getName(), activityResponses);
                })
                .toList();
        log.info("[Backend|ActivityService] FindAllActivities - Success for userId: {}. Found {} groups.", userId, response.size());
        return response;
    }

    @Transactional
    public ActivityResponse updateActivity(Long userId, Long activityId, UpdateActivityRequest request) {
        log.info("[Backend|ActivityService] UpdateActivity - Attempt for userId: {} on activityId: {}", userId, activityId);
        Activity activity = findActivityById(activityId);
        validateActivityOwner(userId, activity);

        if (!activity.getName().equals(request.name()) && activityRepository.existsByCategoryAndName(activity.getCategory(), request.name())) {
            log.warn("[Backend|ActivityService] UpdateActivity - Failed: Duplicate activity name '{}' for userId: {}", request.name(), userId);
            throw new BusinessException(ErrorCode.ACTIVITY_NAME_DUPLICATE);
        }

        activity.update(request.name());
        log.info("[Backend|ActivityService] UpdateActivity - Success for userId: {} on activityId: {}", userId, activityId);
        return ActivityResponse.from(activity);
    }

    @Transactional
    public void deleteActivity(Long userId, Long activityId) {
        log.info("[Backend|ActivityService] DeleteActivity - Attempt for userId: {} on activityId: {}", userId, activityId);
        Activity activity = findActivityById(activityId);
        validateActivityOwner(userId, activity);

        if (timeBlockRepository.existsByActivity(activity)) {
            log.warn("[Backend|ActivityService] DeleteActivity - Failed: Activity in use for activityId: {}", activityId);
            throw new BusinessException(ErrorCode.ACTIVITY_IN_USE);
        }

        activityRepository.delete(activity);
        log.info("[Backend|ActivityService] DeleteActivity - Success for userId: {} on activityId: {}", userId, activityId);
    }

    private Activity findActivityById(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> {
                    log.warn("[Backend|ActivityService] FindActivityById - Failed: Activity not found for id: {}", activityId);
                    return new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
                });
    }

    private void validateActivityOwner(Long userId, Activity activity) {
        if (!activity.getUser().getId().equals(userId)) {
            log.warn("[Backend|ActivityService] ValidateActivityOwner - Failed: Permission denied for userId: {} on activityId: {}", userId, activity.getId());
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
    }
}