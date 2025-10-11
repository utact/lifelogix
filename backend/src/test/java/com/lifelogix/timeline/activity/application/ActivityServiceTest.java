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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TimeBlockRepository timeBlockRepository;
    @InjectMocks
    private ActivityService activityService;

    @Nested
    @DisplayName("활동 생성")
    class CreateActivityTest {

        @Test
        @DisplayName("성공")
        void 활동을_성공적으로_생성한다() {
            // given
            Long userId = 1L;
            Long categoryId = 10L;
            var request = new CreateActivityRequest("새로운 활동", categoryId);
            User fakeUser = User.builder().id(userId).build();
            Category fakeCategory = new Category(categoryId, "업무", "#123", fakeUser, null);

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
        @DisplayName("실패 - 권한 없는 카테고리")
        void 다른_사람의_카테고리로는_활동을_생성할_수_없다() {
            // given
            Long myUserId = 1L;
            Long otherUserId = 2L;
            Long otherUserCategoryId = 10L;
            var request = new CreateActivityRequest("새로운 활동", otherUserCategoryId);
            User fakeMe = User.builder().id(myUserId).build();
            User fakeOtherUser = User.builder().id(otherUserId).build();
            Category fakeOtherUsersCategory = new Category(otherUserCategoryId, "남의 카테고리", "#123", fakeOtherUser, null);

            when(userRepository.findById(myUserId)).thenReturn(Optional.of(fakeMe));
            when(categoryRepository.findById(otherUserCategoryId)).thenReturn(Optional.of(fakeOtherUsersCategory));

            // when & then
            assertThatThrownBy(() -> activityService.createActivity(myUserId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PERMISSION_DENIED);
        }
    }

    @Nested
    @DisplayName("활동 조회")
    class FindActivityTest {
        @Test
        @DisplayName("성공 - 카테고리별 그룹화")
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
            assertThat(responses).hasSize(2);
            // 순서가 보장되지 않으므로, stream을 사용하여 내용을 검증
            assertThat(responses.stream().map(ActivitiesByCategoryResponse::categoryName)).contains("업무", "운동");
        }
    }

    @Nested
    @DisplayName("활동 수정")
    class UpdateActivityTest {
        @Test
        @DisplayName("성공")
        void 자신의_활동_이름을_성공적으로_수정한다() {
            // given
            Long userId = 1L;
            Long activityId = 2L;
            var request = new UpdateActivityRequest("수정된 활동 이름");
            User fakeUser = User.builder().id(userId).build();
            Category fakeCategory = new Category(10L, "업무", "#123", fakeUser, null);
            Activity myActivity = new Activity(activityId, "원본 활동 이름", fakeUser, fakeCategory);

            when(activityRepository.findById(activityId)).thenReturn(Optional.of(myActivity));

            // when
            ActivityResponse response = activityService.updateActivity(userId, activityId, request);

            // then
            assertThat(response.name()).isEqualTo("수정된 활동 이름");
        }
    }

    @Nested
    @DisplayName("활동 삭제")
    class DeleteActivityTest {
        @Test
        @DisplayName("성공")
        void 자신의_활동을_성공적으로_삭제한다() {
            // given
            Long userId = 1L;
            Long activityId = 2L;
            User fakeUser = User.builder().id(userId).build();
            Category fakeCategory = new Category(10L, "업무", "#123", fakeUser, null);
            Activity myActivity = new Activity(activityId, "삭제될 활동", fakeUser, fakeCategory);

            when(activityRepository.findById(activityId)).thenReturn(Optional.of(myActivity));
            when(timeBlockRepository.existsByActivity(myActivity)).thenReturn(false);

            // when
            activityService.deleteActivity(userId, activityId);

            // then
            verify(activityRepository).delete(myActivity);
        }

        @Test
        @DisplayName("실패 - 타임블록 존재")
        void 타임블록에_사용중인_활동은_삭제할_수_없다() {
            // given
            Long userId = 1L;
            Long activityId = 2L;
            User fakeUser = User.builder().id(userId).build();
            Category fakeCategory = new Category(10L, "업무", "#123", fakeUser, null);
            Activity myActivity = new Activity(activityId, "삭제될 활동", fakeUser, fakeCategory);

            when(activityRepository.findById(activityId)).thenReturn(Optional.of(myActivity));
            when(timeBlockRepository.existsByActivity(myActivity)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> activityService.deleteActivity(userId, activityId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ACTIVITY_IN_USE);
        }
    }
}