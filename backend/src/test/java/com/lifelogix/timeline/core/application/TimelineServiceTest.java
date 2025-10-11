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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimelineServiceTest {

    @Mock
    private TimeBlockRepository timeBlockRepository;
    @Mock
    private ActivityRepository activityRepository;
    @InjectMocks
    private TimelineService timelineService;

    private User fakeUser;
    private Category fakeCategory;

    @BeforeEach
    void setUp() {
        fakeUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("p")
                .username("tester")
                .build();

        fakeCategory = new Category(100L, "업무", "#123", fakeUser, null);
    }

    @Nested
    @DisplayName("타임블록 생성/수정")
    class CreateOrUpdateTimeBlockTest {

        @Test
        @DisplayName("성공 - 새로운 블록 생성")
        void 타임블록을_성공적으로_생성한다() {
            // given
            var request = new CreateTimeBlockRequest(LocalDate.now(), LocalTime.of(10, 0), TimeBlockType.ACTUAL, 10L);
            Activity fakeActivity = new Activity(10L, "코딩", fakeUser, fakeCategory);

            when(activityRepository.findById(10L)).thenReturn(Optional.of(fakeActivity));
            when(timeBlockRepository.findUserTimeBlockForSlot(fakeUser.getId(), request.date(), request.startTime(), request.type())).thenReturn(Optional.empty());
            when(timeBlockRepository.save(any(TimeBlock.class))).thenAnswer(invocation -> {
                TimeBlock block = invocation.getArgument(0);
                return new TimeBlock(1L, block.getDate(), block.getStartTime(), block.getType(), block.getActivity());
            });

            // when
            BlockDetailResponse response = timelineService.createOrUpdateTimeBlock(fakeUser.getId(), request);

            // then
            assertThat(response.activityId()).isEqualTo(10L);
            assertThat(response.activityName()).isEqualTo("코딩");
        }

        @Test
        @DisplayName("성공 - 기존 블록 활동 업데이트")
        void 기존_타임블록의_활동을_성공적으로_수정한다() {
            // given
            var request = new CreateTimeBlockRequest(LocalDate.now(), LocalTime.of(10, 0), TimeBlockType.ACTUAL, 20L);
            Activity originalActivity = new Activity(10L, "원래 활동", fakeUser, fakeCategory);
            Activity newActivity = new Activity(20L, "새로운 활동", fakeUser, fakeCategory);
            TimeBlock existingBlock = new TimeBlock(1L, request.date(), request.startTime(), request.type(), originalActivity);

            when(activityRepository.findById(20L)).thenReturn(Optional.of(newActivity));
            when(timeBlockRepository.findUserTimeBlockForSlot(fakeUser.getId(), request.date(), request.startTime(), request.type())).thenReturn(Optional.of(existingBlock));

            // when
            BlockDetailResponse response = timelineService.createOrUpdateTimeBlock(fakeUser.getId(), request);

            // then
            assertThat(existingBlock.getActivity().getName()).isEqualTo("새로운 활동");
            assertThat(response.activityName()).isEqualTo("새로운 활동");
        }

        @Test
        @DisplayName("실패 - 권한 없는 활동")
        void 다른_사람의_활동으로는_타임블록을_생성할_수_없다() {
            // given
            Long myUserId = 1L;
            Long otherUserId = 2L;
            var request = new CreateTimeBlockRequest(LocalDate.now(), LocalTime.of(10, 0), TimeBlockType.ACTUAL, 10L);
            User fakeOtherUser = User.builder().id(otherUserId).build();
            Activity fakeOtherUserActivity = new Activity(10L, "남의 활동", fakeOtherUser, null);

            when(activityRepository.findById(10L)).thenReturn(Optional.of(fakeOtherUserActivity));

            // when & then
            assertThatThrownBy(() -> timelineService.createOrUpdateTimeBlock(myUserId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PERMISSION_DENIED);
        }
    }

    @Nested
    @DisplayName("타임라인 조회")
    class GetTimelineTest {
        @Test
        @DisplayName("성공")
        void 특정_날짜의_타임라인을_성공적으로_조회한다() {
            // given
            LocalDate testDate = LocalDate.of(2025, 10, 7);
            Activity codingActivity = new Activity(101L, "코딩", fakeUser, fakeCategory);
            List<TimeBlock> timeBlocksFromDb = List.of(
                    new TimeBlock(1L, testDate, LocalTime.of(10, 0), TimeBlockType.ACTUAL, codingActivity),
                    new TimeBlock(2L, testDate, LocalTime.of(10, 0), TimeBlockType.PLAN, codingActivity)
            );

            when(timeBlockRepository.findByUserIdAndDate(fakeUser.getId(), testDate)).thenReturn(timeBlocksFromDb);

            // when
            TimelineResponse response = timelineService.getDailyTimeline(fakeUser.getId(), testDate);

            // then
            assertThat(response.date()).isEqualTo(testDate);
            assertThat(response.timeBlocks()).hasSize(1);
            assertThat(response.timeBlocks().get(0).plan()).isNotNull();
            assertThat(response.timeBlocks().get(0).actual()).isNotNull();
        }
    }

    @Nested
    @DisplayName("타임블록 수정")
    class UpdateTimeBlockTest {
        @Test
        @DisplayName("성공")
        void 자신의_타임블록을_성공적으로_수정한다() {
            // given
            Long timeBlockId = 1L;
            Activity originalActivity = new Activity(10L, "원래 활동", fakeUser, fakeCategory);
            Activity newActivity = new Activity(20L, "새로운 활동", fakeUser, fakeCategory);
            var request = new UpdateTimeBlockRequest(newActivity.getId());
            TimeBlock myTimeBlock = new TimeBlock(timeBlockId, LocalDate.now(), LocalTime.now(), TimeBlockType.ACTUAL, originalActivity);

            when(timeBlockRepository.findById(timeBlockId)).thenReturn(Optional.of(myTimeBlock));
            when(activityRepository.findById(newActivity.getId())).thenReturn(Optional.of(newActivity));

            // when
            BlockDetailResponse response = timelineService.updateTimeBlock(fakeUser.getId(), timeBlockId, request);

            // then
            assertThat(myTimeBlock.getActivity().getName()).isEqualTo("새로운 활동");
            assertThat(response.activityName()).isEqualTo("새로운 활동");
        }
    }

    @Nested
    @DisplayName("타임블록 삭제")
    class DeleteTimeBlockTest {
        @Test
        @DisplayName("성공")
        void 자신의_타임블록을_성공적으로_삭제한다() {
            // given
            Long timeBlockId = 2L;
            Activity myActivity = new Activity(10L, "내 활동", fakeUser, fakeCategory);
            TimeBlock myTimeBlock = new TimeBlock(timeBlockId, LocalDate.now(), LocalTime.now(), TimeBlockType.ACTUAL, myActivity);

            when(timeBlockRepository.findById(timeBlockId)).thenReturn(Optional.of(myTimeBlock));

            // when
            timelineService.deleteTimeBlock(fakeUser.getId(), timeBlockId);

            // then
            verify(timeBlockRepository).delete(myTimeBlock);
        }
    }
}