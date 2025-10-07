package com.lifelogix.timeline.core.application;

import com.lifelogix.timeline.activity.domain.Activity;
import com.lifelogix.timeline.activity.domain.ActivityRepository;
import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.timeline.core.api.dto.request.CreateTimeBlockRequest;
import com.lifelogix.timeline.core.api.dto.response.BlockDetailResponse;
import com.lifelogix.timeline.core.api.dto.response.TimelineResponse;
import com.lifelogix.timeline.core.domain.TimeBlock;
import com.lifelogix.timeline.core.domain.TimeBlockRepository;
import com.lifelogix.timeline.core.domain.TimeBlockType;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimelineServiceTest {

    @Mock
    private TimeBlockRepository timeBlockRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TimelineService timelineService;

    @Test
    @DisplayName("타임블록 생성")
    void 타임블록을_성공적으로_생성한다() {
        // given
        Long userId = 1L;
        Long activityId = 10L;
        var request = new CreateTimeBlockRequest(
                LocalDate.of(2025, 10, 7),
                LocalTime.of(10, 0),
                TimeBlockType.ACTUAL,
                activityId
        );

        User fakeUser = new User(userId, "test@test.com", "p", "tester");
        Category fakeCategory = new Category(100L, "업무", "#123", fakeUser, null);
        Activity fakeActivity = new Activity(activityId, "코딩", fakeUser, fakeCategory);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(fakeActivity));
        when(timeBlockRepository.save(any(TimeBlock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        BlockDetailResponse response = timelineService.createTimeBlock(userId, request);

        // then
        assertThat(response.activityId()).isEqualTo(activityId);
        assertThat(response.activityName()).isEqualTo("코딩");
        assertThat(response.categoryName()).isEqualTo("업무");
    }

    @Test
    @DisplayName("일별 타임라인 조회")
    void 특정_날짜의_타임라인을_성공적으로_조회한다() {
        // given
        Long userId = 1L;
        LocalDate testDate = LocalDate.of(2025, 10, 7);
        User fakeUser = new User(userId, "test@test.com", "p", "tester");
        Category workCategory = new Category(10L, "업무", "#111", fakeUser, null);
        Category healthCategory = new Category(11L, "운동", "#222", fakeUser, null);

        Activity codingActivity = new Activity(101L, "코딩", fakeUser, workCategory);
        Activity meetingActivity = new Activity(102L, "회의", fakeUser, workCategory);
        Activity gymActivity = new Activity(103L, "헬스", fakeUser, healthCategory);

        // DB에서 조회된, 정렬되지 않은 TimeBlock 목록을 시뮬레이션
        List<TimeBlock> timeBlocksFromDb = List.of(
                new TimeBlock(testDate, LocalTime.of(10, 0), TimeBlockType.ACTUAL, codingActivity),
                new TimeBlock(testDate, LocalTime.of(9, 30), TimeBlockType.PLAN, meetingActivity),
                new TimeBlock(testDate, LocalTime.of(10, 0), TimeBlockType.PLAN, codingActivity),
                new TimeBlock(testDate, LocalTime.of(18, 0), TimeBlockType.ACTUAL, gymActivity)
        );

        when(timeBlockRepository.findByActivity_User_IdAndDate(userId, testDate)).thenReturn(timeBlocksFromDb);

        // when
        TimelineResponse response = timelineService.getDailyTimeline(userId, testDate);

        // then
        assertThat(response.date()).isEqualTo(testDate);
        // 9:30, 10:00, 18:00 세 개의 시간 슬롯이 반환되기를 기대
        assertThat(response.timeBlocks()).hasSize(3);

        // 10:00 시간 슬롯 검증 (PLAN과 ACTUAL이 모두 존재)
        var tenAmBlock = response.timeBlocks().stream()
                .filter(tb -> tb.startTime().equals(LocalTime.of(10, 0)))
                .findFirst().orElseThrow();

        assertThat(tenAmBlock.plan()).isNotNull();
        assertThat(tenAmBlock.plan().activityName()).isEqualTo("코딩");
        assertThat(tenAmBlock.actual()).isNotNull();
        assertThat(tenAmBlock.actual().activityName()).isEqualTo("코딩");

        // 18:00 시간 슬롯 검증 (ACTUAL만 존재)
        var sixPmBlock = response.timeBlocks().stream()
                .filter(tb -> tb.startTime().equals(LocalTime.of(18, 0)))
                .findFirst().orElseThrow();

        assertThat(sixPmBlock.plan()).isNull();
        assertThat(sixPmBlock.actual()).isNotNull();
        assertThat(sixPmBlock.actual().activityName()).isEqualTo("헬스");
    }
}