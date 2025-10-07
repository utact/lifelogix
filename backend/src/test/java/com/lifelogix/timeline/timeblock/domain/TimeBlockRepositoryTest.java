package com.lifelogix.timeline.timeblock.domain;

import com.lifelogix.timeline.activity.domain.Activity;
import com.lifelogix.timeline.activity.domain.ActivityRepository;
import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.timeline.category.domain.CategoryRepository;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class TimeBlockRepositoryTest {

    @Autowired
    private TimeBlockRepository timeBlockRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Activity savedActivity;

    @BeforeEach
    void setUp() {
        User user = User.builder().email("user@test.com").password("p").username("u").build();
        User savedUser = userRepository.save(user);

        Category category = new Category("업무", "#123456", savedUser, null);
        Category savedCategory = categoryRepository.save(category);

        Activity activity = new Activity("코딩", savedUser, savedCategory);
        savedActivity = activityRepository.save(activity);
    }

    @Test
    @DisplayName("타임블록 저장 및 조회")
    void 타임블록을_저장하고_조회하면_활동_정보가_올바르게_연결된다() {
        // given
        LocalDate date = LocalDate.of(2025, 10, 7);
        LocalTime startTime = LocalTime.of(15, 0);
        TimeBlock timeBlock = new TimeBlock(date, startTime, TimeBlockType.ACTUAL, savedActivity);

        // when
        TimeBlock savedTimeBlock = timeBlockRepository.save(timeBlock);
        TimeBlock foundTimeBlock = timeBlockRepository.findById(savedTimeBlock.getId()).orElseThrow();

        // then
        assertThat(foundTimeBlock.getDate()).isEqualTo(date);
        assertThat(foundTimeBlock.getStartTime()).isEqualTo(startTime);
        assertThat(foundTimeBlock.getType()).isEqualTo(TimeBlockType.ACTUAL);
        assertThat(foundTimeBlock.getActivity().getId()).isEqualTo(savedActivity.getId());
    }
}