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
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityService 단위 테스트")
class ActivityServiceTest {

    @InjectMocks
    private ActivityService activityService;

    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TimeBlockRepository timeBlockRepository;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User(1L, "test@example.com", "password", "tester", null);
        category = new Category(10L, "운동", "#123456", user, null);
    }

    @Nested
    @DisplayName("활동 생성")
    class CreateActivity {
        @Test
        @DisplayName("성공")
        void create_success() {
            // given
            CreateActivityRequest request = new CreateActivityRequest("달리기", category.getId());
            Activity newActivity = new Activity(request.name(), user, category);

            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(categoryRepository.findById(request.categoryId())).willReturn(Optional.of(category));
            given(activityRepository.existsByCategoryAndName(category, request.name())).willReturn(false);
            given(activityRepository.save(any(Activity.class))).willReturn(newActivity);

            // when
            ActivityResponse response = activityService.createActivity(user.getId(), request);

            // then
            assertThat(response.name()).isEqualTo(request.name());
            then(activityRepository).should().save(any(Activity.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리")
        void create_fail_categoryNotFound() {
            // given
            CreateActivityRequest request = new CreateActivityRequest("달리기", 999L);
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(categoryRepository.findById(request.categoryId())).willReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> activityService.createActivity(user.getId(), request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 카테고리에 생성 시도")
        void create_fail_permissionDenied() {
            // given
            User otherUser = new User(2L, "other@test.com", "pw", "other", null);
            Category otherUserCategory = new Category(11L, "공부", "#FFFFFF", otherUser, null);
            CreateActivityRequest request = new CreateActivityRequest("달리기", otherUserCategory.getId());

            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(categoryRepository.findById(request.categoryId())).willReturn(Optional.of(otherUserCategory));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> activityService.createActivity(user.getId(), request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED);
        }

        @Test
        @DisplayName("실패 - 활동 이름 중복")
        void create_fail_duplicateName() {
            // given
            CreateActivityRequest request = new CreateActivityRequest("달리기", category.getId());
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(categoryRepository.findById(request.categoryId())).willReturn(Optional.of(category));
            given(activityRepository.existsByCategoryAndName(category, request.name())).willReturn(true);

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> activityService.createActivity(user.getId(), request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACTIVITY_NAME_DUPLICATE);
        }
    }

    @Nested
    @DisplayName("카테고리별 활동 그룹 조회")
    class FindAllActivities {
        @Test
        @DisplayName("성공")
        void findAll_success() {
            // given
            Category studyCategory = new Category(11L, "공부", "#FFFFFF", user, null);
            Activity activity1 = new Activity(1L, "달리기", user, category);
            Activity activity2 = new Activity(2L, "수영", user, category);
            Activity activity3 = new Activity(3L, "코딩", user, studyCategory);
            given(activityRepository.findByUserIdOrderByCategory(user.getId())).willReturn(List.of(activity1, activity2, activity3));

            // when
            List<ActivitiesByCategoryResponse> responses = activityService.findAllActivitiesGroupedByCategory(user.getId());

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).activities()).hasSize(2); // 운동 카테고리
            assertThat(responses.get(1).activities()).hasSize(1); // 공부 카테고리
        }
    }

    @Nested
    @DisplayName("활동 수정")
    class UpdateActivity {
        @Test
        @DisplayName("성공")
        void update_success() {
            // given
            UpdateActivityRequest request = new UpdateActivityRequest("빠르게 달리기");
            Activity activity = new Activity(1L, "달리기", user, category);
            given(activityRepository.findById(activity.getId())).willReturn(Optional.of(activity));
            given(activityRepository.existsByCategoryAndName(category, request.name())).willReturn(false);

            // when
            ActivityResponse response = activityService.updateActivity(user.getId(), activity.getId(), request);

            // then
            assertThat(response.name()).isEqualTo("빠르게 달리기");
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 활동")
        void update_fail_permissionDenied() {
            // given
            UpdateActivityRequest request = new UpdateActivityRequest("빠르게 달리기");
            Activity activity = new Activity(1L, "달리기", user, category);
            Long otherUserId = 99L;
            given(activityRepository.findById(activity.getId())).willReturn(Optional.of(activity));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> activityService.updateActivity(otherUserId, activity.getId(), request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED);
        }
    }

    @Nested
    @DisplayName("활동 삭제")
    class DeleteActivity {
        @Test
        @DisplayName("성공")
        void delete_success() {
            // given
            Activity activity = new Activity(1L, "달리기", user, category);
            given(activityRepository.findById(activity.getId())).willReturn(Optional.of(activity));
            given(timeBlockRepository.existsByActivity(activity)).willReturn(false);
            willDoNothing().given(activityRepository).delete(activity);

            // when
            activityService.deleteActivity(user.getId(), activity.getId());

            // then
            then(activityRepository).should().delete(activity);
        }

        @Test
        @DisplayName("실패 - 활동이 사용 중")
        void delete_fail_activityInUse() {
            // given
            Activity activity = new Activity(1L, "달리기", user, category);
            given(activityRepository.findById(activity.getId())).willReturn(Optional.of(activity));
            given(timeBlockRepository.existsByActivity(activity)).willReturn(true);

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> activityService.deleteActivity(user.getId(), activity.getId()));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACTIVITY_IN_USE);
        }
    }
}