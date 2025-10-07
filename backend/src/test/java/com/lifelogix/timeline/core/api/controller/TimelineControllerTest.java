package com.lifelogix.timeline.core.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelogix.config.jwt.JwtTokenProvider;
import com.lifelogix.timeline.activity.domain.Activity;
import com.lifelogix.timeline.activity.domain.ActivityRepository;
import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.timeline.category.domain.CategoryRepository;
import com.lifelogix.timeline.core.api.dto.request.CreateTimeBlockRequest;
import com.lifelogix.timeline.core.domain.TimeBlock;
import com.lifelogix.timeline.core.domain.TimeBlockRepository;
import com.lifelogix.timeline.core.domain.TimeBlockType;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TimelineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TimeBlockRepository timeBlockRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String accessToken;
    private Activity testActivity;

    @BeforeEach
    void setUp() {
        testUser = User.builder().email("timeline@test.com").password("p").username("timelineuser").build();
        userRepository.save(testUser);

        accessToken = jwtTokenProvider.generateToken(testUser);

        Category testCategory = new Category("자기계발", "#8E44AD", testUser, null);
        categoryRepository.save(testCategory);

        testActivity = new Activity("알고리즘 공부", testUser, testCategory);
        activityRepository.save(testActivity);
    }

    @Test
    @DisplayName("타임블록 생성 API")
    void 타임블록을_성공적으로_기록한다() throws Exception {
        // given
        var request = new CreateTimeBlockRequest(
                LocalDate.of(2025, 10, 7),
                LocalTime.of(22, 30),
                TimeBlockType.ACTUAL,
                testActivity.getId()
        );

        // when & then
        mockMvc.perform(post("/api/v1/timeline/block")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activityId").value(testActivity.getId()))
                .andExpect(jsonPath("$.activityName").value("알고리즘 공부"))
                .andExpect(jsonPath("$.categoryName").value("자기계발"));
    }

    @Test
    @DisplayName("일별 타임라인 조회 API")
    void 특정_날짜의_타임라인을_성공적으로_조회한다() throws Exception {
        // given
        LocalDate testDate = LocalDate.of(2025, 10, 7);
        LocalTime testTime = LocalTime.of(14, 0);

        TimeBlock planBlock = new TimeBlock(testDate, testTime, TimeBlockType.PLAN, testActivity);
        TimeBlock actualBlock = new TimeBlock(testDate, testTime, TimeBlockType.ACTUAL, testActivity);
        timeBlockRepository.save(planBlock);
        timeBlockRepository.save(actualBlock);

        // when & then
        mockMvc.perform(get("/api/v1/timeline")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("date", testDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.timeBlocks.length()").value(1))
                .andExpect(jsonPath("$.timeBlocks[0].startTime").value("14:00:00"))
                .andExpect(jsonPath("$.timeBlocks[0].plan.activityName").value("알고리즘 공부"))
                .andExpect(jsonPath("$.timeBlocks[0].actual.activityName").value("알고리즘 공부"));
    }
}