package com.lifelogix.timeline.activity.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelogix.config.SecurityConfig;
import com.lifelogix.config.jwt.JwtProperties;
import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.timeline.activity.api.dto.request.CreateActivityRequest;
import com.lifelogix.timeline.activity.api.dto.request.UpdateActivityRequest;
import com.lifelogix.timeline.activity.api.dto.response.ActivitiesByCategoryResponse;
import com.lifelogix.timeline.activity.api.dto.response.ActivityResponse;
import com.lifelogix.timeline.activity.application.ActivityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityController.class)
@Import({SecurityConfig.class, ActivityControllerTest.TestConfig.class})
@DisplayName("ActivityController 통합 테스트")
class ActivityControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtProperties jwtProperties() {
            JwtProperties mockProperties = mock(JwtProperties.class);
            given(mockProperties.getSecret()).willReturn("bGlmZWxvZ2l4LWp3dC1zZWNyZXQta2V5LWZvci10ZXN0LWVudmlyb25tZW50Cg==");
            return mockProperties;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActivityService activityService;

    private final Long userId = 1L;

    @Nested
    @DisplayName("POST /api/v1/activities - 활동 생성")
    class CreateActivity {
        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공 - 201 Created")
        void create_success() throws Exception {
            // given
            CreateActivityRequest request = new CreateActivityRequest("달리기", 10L);
            ActivityResponse response = new ActivityResponse(1L, "달리기");
            given(activityService.createActivity(eq(userId), any(CreateActivityRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/activities")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/v1/activities/1"));
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패 - 중복된 이름으로 409 Conflict")
        void create_fail_duplicateName() throws Exception {
            // given
            CreateActivityRequest request = new CreateActivityRequest("달리기", 10L);
            given(activityService.createActivity(eq(userId), any(CreateActivityRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.ACTIVITY_NAME_DUPLICATE));

            // when & then
            mockMvc.perform(post("/api/v1/activities")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/activities - 카테고리별 활동 조회")
    class GetAllActivities {
        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공 - 200 OK")
        void getAll_success() throws Exception {
            // given
            List<ActivitiesByCategoryResponse> responses = Collections.singletonList(
                    new ActivitiesByCategoryResponse(10L, "운동", List.of(new ActivityResponse(1L, "달리기")))
            );
            given(activityService.findAllActivitiesGroupedByCategory(userId)).willReturn(responses);

            // when & then
            mockMvc.perform(get("/api/v1/activities"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].categoryName").value("운동"))
                    .andExpect(jsonPath("$[0].activities[0].name").value("달리기"));
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자")
        void getAll_fail_unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/v1/activities"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/activities/{activityId} - 활동 수정")
    class UpdateActivity {
        private final Long activityId = 1L;

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공 - 200 OK")
        void update_success() throws Exception {
            // given
            UpdateActivityRequest request = new UpdateActivityRequest("빠르게 달리기");
            ActivityResponse response = new ActivityResponse(activityId, "빠르게 달리기");
            given(activityService.updateActivity(eq(userId), eq(activityId), any(UpdateActivityRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(put("/api/v1/activities/{activityId}", activityId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("빠르게 달리기"));
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패 - 권한 없음으로 403 Forbidden")
        void update_fail_permissionDenied() throws Exception {
            // given
            UpdateActivityRequest request = new UpdateActivityRequest("빠르게 달리기");
            given(activityService.updateActivity(eq(userId), eq(activityId), any(UpdateActivityRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.PERMISSION_DENIED));

            // when & then
            mockMvc.perform(put("/api/v1/activities/{activityId}", activityId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/activities/{activityId} - 활동 삭제")
    class DeleteActivity {
        private final Long activityId = 1L;

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공 - 204 No Content")
        void delete_success() throws Exception {
            // given
            willDoNothing().given(activityService).deleteActivity(userId, activityId);

            // when & then
            mockMvc.perform(delete("/api/v1/activities/{activityId}", activityId).with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패 - 활동이 사용 중으로 400 Bad Request")
        void delete_fail_activityInUse() throws Exception {
            // given
            doThrow(new BusinessException(ErrorCode.ACTIVITY_IN_USE))
                    .when(activityService).deleteActivity(userId, activityId);

            // when & then
            mockMvc.perform(delete("/api/v1/activities/{activityId}", activityId).with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }
}