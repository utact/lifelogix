package com.lifelogix.timeline.core.application;

import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.timeline.activity.domain.Activity;
import com.lifelogix.timeline.activity.domain.ActivityRepository;
import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.timeline.core.api.dto.request.CreateTimeBlockRequest;
import com.lifelogix.timeline.core.api.dto.request.UpdateTimeBlockRequest;
import com.lifelogix.timeline.core.api.dto.response.BlockDetailResponse;
import com.lifelogix.timeline.core.api.dto.response.TimelineResponse;
import com.lifelogix.timeline.core.domain.TimeBlock;
import com.lifelogix.timeline.core.domain.TimeBlockRepository;
import com.lifelogix.timeline.core.domain.TimeBlockType;
import com.lifelogix.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimelineService 단위 테스트")
class TimelineServiceTest {

    @InjectMocks
    private TimelineService timelineService;

    @Mock
    private TimeBlockRepository timeBlockRepository;

    @Mock
    private ActivityRepository activityRepository;

    private User user;
    private Category category;
    private Activity activity;
    private Activity anotherActivity;

    @BeforeEach
    void setUp() {
        user = new User(1L, "test@example.com", "password", "tester", null);
        category = new Category(100L, "운동", "#111111", user, null);
        Category anotherCategory = new Category(200L, "공부", "#222222", user, null);
        activity = new Activity(10L, "달리기", user, category);
        anotherActivity = new Activity(11L, "코딩", user, anotherCategory);
    }

    @Nested
    @DisplayName("타임블록 생성 또는 수정")
    class CreateOrUpdateTimeBlock {

        @Test
        @DisplayName("성공 - 새로운 타임블록 생성")
        void create_success() {
            // given
            CreateTimeBlockRequest request = new CreateTimeBlockRequest(LocalDate.now(), LocalTime.of(9, 0), TimeBlockType.PLAN, activity.getId());
            given(activityRepository.findById(request.activityId())).willReturn(Optional.of(activity));
            given(timeBlockRepository.findUserTimeBlockForSlot(user.getId(), request.date(), request.startTime(), request.type())).willReturn(Optional.empty());

            // when
            timelineService.createOrUpdateTimeBlock(user.getId(), request);

            // then
            then(timeBlockRepository).should().save(any(TimeBlock.class));
        }

        @Test
        @DisplayName("성공 - 기존 타임블록 활동 업데이트")
        void update_success() {
            // given
            CreateTimeBlockRequest request = new CreateTimeBlockRequest(LocalDate.now(), LocalTime.of(9, 0), TimeBlockType.PLAN, activity.getId());
            TimeBlock existingBlock = new TimeBlock(1L, request.date(), request.startTime(), request.type(), anotherActivity);
            given(activityRepository.findById(request.activityId())).willReturn(Optional.of(activity));
            given(timeBlockRepository.findUserTimeBlockForSlot(user.getId(), request.date(), request.startTime(), request.type())).willReturn(Optional.of(existingBlock));

            // when
            BlockDetailResponse response = timelineService.createOrUpdateTimeBlock(user.getId(), request);

            // then
            assertThat(response.activityId()).isEqualTo(activity.getId());
            then(timeBlockRepository).should(never()).save(any(TimeBlock.class));
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 활동으로 기록 시도")
        void create_fail_permissionDenied() {
            // given
            User otherUser = new User(2L, "other@test.com", "pw", "other", null);
            Activity otherActivity = new Activity(12L, "독서", otherUser, category);
            CreateTimeBlockRequest permissionRequest = new CreateTimeBlockRequest(LocalDate.now(), LocalTime.of(9, 0), TimeBlockType.PLAN, otherActivity.getId());
            given(activityRepository.findById(permissionRequest.activityId())).willReturn(Optional.of(otherActivity));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> timelineService.createOrUpdateTimeBlock(user.getId(), permissionRequest));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED);
        }
    }

    @Nested
    @DisplayName("일일 타임라인 조회")
    class GetDailyTimeline {
        @Test
        @DisplayName("성공")
        void get_success() {
            // given
            LocalDate date = LocalDate.now();
            TimeBlock planBlock = new TimeBlock(1L, date, LocalTime.of(9, 0), TimeBlockType.PLAN, activity);
            TimeBlock actualBlock = new TimeBlock(2L, date, LocalTime.of(9, 0), TimeBlockType.ACTUAL, anotherActivity);
            given(timeBlockRepository.findByUserIdAndDate(user.getId(), date)).willReturn(List.of(planBlock, actualBlock));

            // when
            TimelineResponse response = timelineService.getDailyTimeline(user.getId(), date);

            // then
            assertThat(response.timeBlocks()).hasSize(1);
            assertThat(response.timeBlocks().get(0).startTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(response.timeBlocks().get(0).plan().activityId()).isEqualTo(activity.getId());
            assertThat(response.timeBlocks().get(0).actual().activityId()).isEqualTo(anotherActivity.getId());
        }
    }

    @Nested
    @DisplayName("타임블록 활동 변경")
    class UpdateTimeBlock {
        @Test
        @DisplayName("성공")
        void update_success() {
            // given
            Long timeBlockId = 1L;
            UpdateTimeBlockRequest request = new UpdateTimeBlockRequest(anotherActivity.getId());
            TimeBlock timeBlock = new TimeBlock(timeBlockId, LocalDate.now(), LocalTime.now(), TimeBlockType.PLAN, activity);

            given(timeBlockRepository.findById(timeBlockId)).willReturn(Optional.of(timeBlock));
            given(activityRepository.findById(request.activityId())).willReturn(Optional.of(anotherActivity));

            // when
            BlockDetailResponse response = timelineService.updateTimeBlock(user.getId(), timeBlockId, request);

            // then
            assertThat(response.activityId()).isEqualTo(anotherActivity.getId());
            assertThat(timeBlock.getActivity()).isEqualTo(anotherActivity);
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 타임블록")
        void update_fail_permissionDenied_onTimeBlock() {
            // given
            Long timeBlockId = 1L;
            User otherUser = new User(2L, "other", "p", "o", null);
            Activity otherActivity = new Activity(12L, "독서", otherUser, category);
            TimeBlock otherUsersBlock = new TimeBlock(timeBlockId, LocalDate.now(), LocalTime.now(), TimeBlockType.PLAN, otherActivity);
            UpdateTimeBlockRequest request = new UpdateTimeBlockRequest(activity.getId());

            given(timeBlockRepository.findById(timeBlockId)).willReturn(Optional.of(otherUsersBlock));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> timelineService.updateTimeBlock(user.getId(), timeBlockId, request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED);
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 활동으로 변경 시도")
        void update_fail_permissionDenied_onActivity() {
            // given
            Long timeBlockId = 1L;
            User otherUser = new User(2L, "other", "p", "o", null);
            Activity otherActivity = new Activity(12L, "독서", otherUser, category);
            TimeBlock timeBlock = new TimeBlock(timeBlockId, LocalDate.now(), LocalTime.now(), TimeBlockType.PLAN, activity);
            UpdateTimeBlockRequest request = new UpdateTimeBlockRequest(otherActivity.getId());

            given(timeBlockRepository.findById(timeBlockId)).willReturn(Optional.of(timeBlock));
            given(activityRepository.findById(request.activityId())).willReturn(Optional.of(otherActivity));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> timelineService.updateTimeBlock(user.getId(), timeBlockId, request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED);
        }
    }


    @Nested
    @DisplayName("타임블록 삭제")
    class DeleteTimeBlock {
        @Test
        @DisplayName("성공")
        void delete_success() {
            // given
            Long timeBlockId = 1L;
            TimeBlock timeBlock = new TimeBlock(timeBlockId, LocalDate.now(), LocalTime.now(), TimeBlockType.PLAN, activity);
            given(timeBlockRepository.findById(timeBlockId)).willReturn(Optional.of(timeBlock));
            willDoNothing().given(timeBlockRepository).delete(timeBlock);

            // when
            timelineService.deleteTimeBlock(user.getId(), timeBlockId);

            // then
            then(timeBlockRepository).should().delete(timeBlock);
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 타임블록")
        void delete_fail_permissionDenied() {
            // given
            Long timeBlockId = 1L;
            User otherUser = new User(2L, "other@test.com", "pw", "other", null);
            Activity otherActivity = new Activity(12L, "독서", otherUser, category);
            TimeBlock timeBlock = new TimeBlock(timeBlockId, LocalDate.now(), LocalTime.now(), TimeBlockType.PLAN, otherActivity);
            given(timeBlockRepository.findById(timeBlockId)).willReturn(Optional.of(timeBlock));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> timelineService.deleteTimeBlock(user.getId(), timeBlockId));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED);
        }
    }
}