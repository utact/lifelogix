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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TimeBlockRepository timeBlockRepository;

    @Transactional
    public ActivityResponse createActivity(Long userId, CreateActivityRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        if (activityRepository.existsByCategoryAndName(category, request.name())) {
            throw new BusinessException(ErrorCode.ACTIVITY_NAME_DUPLICATE);
        }

        Activity newActivity = new Activity(request.name(), user, category);
        Activity savedActivity = activityRepository.save(newActivity);

        return ActivityResponse.from(savedActivity);
    }

    public List<ActivitiesByCategoryResponse> findAllActivitiesGroupedByCategory(Long userId) {
        List<Activity> activities = activityRepository.findByUserIdOrderByCategory(userId);

        Map<Category, List<Activity>> groupedActivities = activities.stream()
                .collect(Collectors.groupingBy(Activity::getCategory));

        return groupedActivities.entrySet().stream()
                .map(entry -> {
                    Category category = entry.getKey();
                    List<ActivityResponse> activityResponses = entry.getValue().stream()
                            .map(ActivityResponse::from)
                            .toList();
                    return new ActivitiesByCategoryResponse(category.getId(), category.getName(), activityResponses);
                })
                .toList();
    }

    @Transactional
    public ActivityResponse updateActivity(Long userId, Long activityId, UpdateActivityRequest request) {
        Activity activity = findActivityById(activityId);
        validateActivityOwner(userId, activity);

        if (!activity.getName().equals(request.name()) && activityRepository.existsByCategoryAndName(activity.getCategory(), request.name())) {
            throw new BusinessException(ErrorCode.ACTIVITY_NAME_DUPLICATE);
        }

        activity.update(request.name());
        return ActivityResponse.from(activity);
    }

    @Transactional
    public void deleteActivity(Long userId, Long activityId) {
        Activity activity = findActivityById(activityId);
        validateActivityOwner(userId, activity);

        if (timeBlockRepository.existsByActivity(activity)) {
            throw new BusinessException(ErrorCode.ACTIVITY_IN_USE);
        }

        activityRepository.delete(activity);
    }

    private Activity findActivityById(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));
    }

    private void validateActivityOwner(Long userId, Activity activity) {
        if (!activity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
    }
}