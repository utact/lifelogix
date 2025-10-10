package com.lifelogix.timeline.activity.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelogix.config.jwt.JwtTokenProvider;
import com.lifelogix.timeline.activity.api.dto.request.CreateActivityRequest;
import com.lifelogix.timeline.activity.api.dto.request.UpdateActivityRequest;
import com.lifelogix.timeline.activity.domain.Activity;
import com.lifelogix.timeline.activity.domain.ActivityRepository;
import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.timeline.category.domain.CategoryRepository;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ActivityControllerTest {

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
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String accessToken;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder().email("activity@test.com").password("p").username("activity user").build();
        userRepository.save(testUser);
        accessToken = jwtTokenProvider.generateAccessToken(testUser); // 👈 generateAccessToken으로 수정
        testCategory = new Category("업무", "#123456", testUser, null);
        categoryRepository.save(testCategory);
    }

    @Nested
    @DisplayName("POST /api/v1/activities")
    class CreateActivity {
        @Test
        @DisplayName("성공")
        void 새로운_활동을_성공적으로_생성한다() throws Exception {
            // given
            var request = new CreateActivityRequest("새로운 회의", testCategory.getId());
            // when & then
            mockMvc.perform(post("/api/v1/activities")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("새로운 회의"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리")
        void 존재하지_않는_카테고리로는_활동을_생성할_수_없다() throws Exception {
            // given
            var request = new CreateActivityRequest("실패할 활동", 999L);
            // when & then
            mockMvc.perform(post("/api/v1/activities")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/activities")
    class GetActivities {
        @Test
        @DisplayName("성공")
        void 카테고리별로_그룹화된_활동_목록을_조회한다() throws Exception {
            // given
            activityRepository.save(new Activity("코딩", testUser, testCategory));
            // when & then
            mockMvc.perform(get("/api/v1/activities")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].categoryName").value("업무"))
                    .andExpect(jsonPath("$[0].activities[0].name").value("코딩"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/activities/{activityId}")
    class UpdateActivity {
        @Test
        @DisplayName("성공")
        void 자신의_활동을_성공적으로_수정한다() throws Exception {
            // given
            Activity myActivity = activityRepository.save(new Activity("원본 활동", testUser, testCategory));
            var request = new UpdateActivityRequest("수정된 활동");

            // when & then
            mockMvc.perform(put("/api/v1/activities/" + myActivity.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("수정된 활동"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/activities/{activityId}")
    class DeleteActivity {
        @Test
        @DisplayName("성공")
        void 자신의_활동을_성공적으로_삭제한다() throws Exception {
            // given
            Activity myActivity = activityRepository.save(new Activity("삭제될 활동", testUser, testCategory));

            // when & then
            mockMvc.perform(delete("/api/v1/activities/" + myActivity.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNoContent());
        }
    }
}