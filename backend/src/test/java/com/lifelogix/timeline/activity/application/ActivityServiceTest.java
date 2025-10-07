package com.lifelogix.timeline.activity.application;

import com.lifelogix.timeline.activity.api.dto.request.CreateActivityRequest;
import com.lifelogix.timeline.activity.api.dto.response.ActivitiesByCategoryResponse;
import com.lifelogix.timeline.activity.api.dto.response.ActivityResponse;
import com.lifelogix.timeline.activity.domain.Activity;
import com.lifelogix.timeline.activity.domain.ActivityRepository;
import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.timeline.category.domain.CategoryRepository;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ActivityService activityService;

    @Test
    @DisplayName("활동 생성")
    void 활동을_성공적으로_생성한다() {
        // given
        Long userId = 1L;
        Long categoryId = 10L;
        var request = new CreateActivityRequest("새로운 활동", categoryId);

        User fakeUser = User.builder().id(userId).build();
        Category fakeCategory = mock(Category.class); // Category Mock 생성

        when(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(fakeCategory));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity activity = invocation.getArgument(0);
            return new Activity(100L, activity.getName(), activity.getUser(), activity.getCategory());
        });

        // when
        ActivityResponse response = activityService.createActivity(userId, request);

        // then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.name()).isEqualTo("새로운 활동");
    }

    @Test
    @DisplayName("활동 목록 조회 (카테고리별 그룹화)")
    void 사용자의_모든_활동을_카테고리별로_그룹화하여_조회한다() {
        // given
        Long userId = 1L;
        User fakeUser = User.builder().id(userId).build();

        Category category1 = new Category(10L, "업무", "#123", fakeUser, null);
        Category category2 = new Category(11L, "운동", "#456", fakeUser, null);

        Activity activity1 = new Activity(101L, "회의", fakeUser, category1);
        Activity activity2 = new Activity(102L, "코딩", fakeUser, category1);
        Activity activity3 = new Activity(103L, "조깅", fakeUser, category2);

        when(activityRepository.findByUserIdOrderByCategory(userId)).thenReturn(List.of(activity1, activity2, activity3));

        // when
        List<ActivitiesByCategoryResponse> responses = activityService.findAllActivitiesGroupedByCategory(userId);

        // then
        assertThat(responses).hasSize(2); // '업무', '운동' 2개의 카테고리 그룹

        // '업무' 카테고리 검증
        ActivitiesByCategoryResponse workCategoryGroup = responses.get(0);
        assertThat(workCategoryGroup.categoryName()).isEqualTo("업무");
        assertThat(workCategoryGroup.activities()).hasSize(2);
        assertThat(workCategoryGroup.activities().get(0).name()).isEqualTo("회의");

        // '운동' 카테고리 검증
        ActivitiesByCategoryResponse exerciseCategoryGroup = responses.get(1);
        assertThat(exerciseCategoryGroup.categoryName()).isEqualTo("운동");
        assertThat(exerciseCategoryGroup.activities()).hasSize(1);
    }

    @Test
    @DisplayName("활동 생성 실패 - 권한 없는 카테고리")
    void 다른_사람의_카테고리로는_활동을_생성할_수_없다() {
        // given
        Long myUserId = 1L;
        Long otherUserId = 2L;
        Long otherUserCategoryId = 10L;
        var request = new CreateActivityRequest("새로운 활동", otherUserCategoryId);

        User fakeMe = User.builder().id(myUserId).build();
        User fakeOtherUser = User.builder().id(otherUserId).build();
        // 다른 사람 소유의 카테고리
        Category fakeOtherUsersCategory = new Category(otherUserCategoryId, "남의 카테고리", "#123", fakeOtherUser, null);

        when(userRepository.findById(myUserId)).thenReturn(Optional.of(fakeMe));
        when(categoryRepository.findById(otherUserCategoryId)).thenReturn(Optional.of(fakeOtherUsersCategory));

        // when & then
        // 내(myUserId)가 남의 카테고리(otherUserCategoryId)로 활동 생성을 시도하면 예외가 발생해야 함
        assertThatThrownBy(() -> activityService.createActivity(myUserId, request))
                .isInstanceOf(IllegalArgumentException.class) // 혹은 더 구체적인 Custom Exception
                .hasMessage("해당 카테고리를 사용할 권한이 없습니다.");
    }
}