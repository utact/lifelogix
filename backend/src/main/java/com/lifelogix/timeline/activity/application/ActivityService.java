package com.lifelogix.timeline.activity.application;

import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.timeline.activity.api.dto.request.CreateActivityRequest;
import com.lifelogix.timeline.activity.api.dto.response.ActivitiesByCategoryResponse;
import com.lifelogix.timeline.activity.api.dto.response.ActivityResponse;
import com.lifelogix.timeline.activity.domain.Activity;
import com.lifelogix.timeline.activity.domain.ActivityRepository;
import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.timeline.category.domain.CategoryRepository;
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

    @Transactional
    public ActivityResponse createActivity(Long userId, CreateActivityRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // 카테고리의 소유자가 없거나(시스템 카테고리), 현재 사용자와 일치하는지 확인
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        Activity newActivity = new Activity(request.name(), user, category);
        Activity savedActivity = activityRepository.save(newActivity);

        return ActivityResponse.from(savedActivity);
    }

    public List<ActivitiesByCategoryResponse> findAllActivitiesGroupedByCategory(Long userId) {
        // 1. 사용자의 모든 활동을 카테고리 기준으로 정렬하여 조회
        List<Activity> activities = activityRepository.findByUserIdOrderByCategory(userId);

        // 2. 조회된 활동들을 Category 객체를 기준으로 그룹핑
        Map<Category, List<Activity>> groupedActivities = activities.stream()
                .collect(Collectors.groupingBy(Activity::getCategory));

        // 3. 그룹핑된 Map을 API 응답 DTO 리스트로 변환
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
}