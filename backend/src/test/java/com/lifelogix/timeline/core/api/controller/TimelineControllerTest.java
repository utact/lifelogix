package com.lifelogix.timeline.core.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelogix.config.SecurityConfig;
import com.lifelogix.config.jwt.JwtProperties;
import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.timeline.core.api.dto.request.CreateTimeBlockRequest;
import com.lifelogix.timeline.core.api.dto.request.UpdateTimeBlockRequest;
import com.lifelogix.timeline.core.api.dto.response.BlockDetailResponse;
import com.lifelogix.timeline.core.api.dto.response.TimeBlockResponse;
import com.lifelogix.timeline.core.api.dto.response.TimelineResponse;
import com.lifelogix.timeline.core.application.TimelineService;
import com.lifelogix.timeline.core.domain.TimeBlockType;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TimelineController.class)
@Import({SecurityConfig.class, TimelineControllerTest.TestConfig.class})
@DisplayName("TimelineController 통합 테스트")
class TimelineControllerTest {

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
    private TimelineService timelineService;

    private final Long userId = 1L;

    @Nested
    @DisplayName("GET /api/v1/timeline - 일일 타임라인 조회")
    class GetDailyTimeline {
        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공 - 200 OK")
        void get_success() throws Exception {
            // given
            LocalDate date = LocalDate.of(2025, 10, 12);
            TimelineResponse response = new TimelineResponse(date, Collections.singletonList(
                    new TimeBlockResponse(LocalTime.of(9, 0), null, null)
            ));
            given(timelineService.getDailyTimeline(userId, date)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/v1/timeline").param("date", date.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.date").value(date.toString()));
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자")
        void get_fail_unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/v1/timeline").param("date", LocalDate.now().toString()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/timeline/block - 타임블록 생성/수정")
    class CreateOrUpdateTimeBlock {
        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공 - 201 Created")
        void create_success() throws Exception {
            // given
            CreateTimeBlockRequest request = new CreateTimeBlockRequest(LocalDate.now(), LocalTime.of(9, 0), TimeBlockType.PLAN, 10L);
            BlockDetailResponse response = new BlockDetailResponse(1L, 10L, "운동", "건강", "#FFFFFF");
            given(timelineService.createOrUpdateTimeBlock(eq(userId), any(CreateTimeBlockRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/timeline/block")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.activityName").value("운동"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/timeline/block/{timeBlockId} - 타임블록 활동 변경")
    class UpdateTimeBlock {
        private final Long timeBlockId = 1L;

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공 - 200 OK")
        void update_success() throws Exception {
            // given
            UpdateTimeBlockRequest request = new UpdateTimeBlockRequest(11L);
            BlockDetailResponse response = new BlockDetailResponse(timeBlockId, 11L, "공부", "자기계발", "#BBBBBB");
            given(timelineService.updateTimeBlock(eq(userId), eq(timeBlockId), any(UpdateTimeBlockRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(put("/api/v1/timeline/block/{timeBlockId}", timeBlockId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.activityName").value("공부"));
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패 - 권한 없음으로 403 Forbidden")
        void update_fail_permissionDenied() throws Exception {
            // given
            UpdateTimeBlockRequest request = new UpdateTimeBlockRequest(11L);
            given(timelineService.updateTimeBlock(eq(userId), eq(timeBlockId), any(UpdateTimeBlockRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.PERMISSION_DENIED));

            // when & then
            mockMvc.perform(put("/api/v1/timeline/block/{timeBlockId}", timeBlockId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/timeline/block/{timeBlockId} - 타임블록 삭제")
    class DeleteTimeBlock {
        private final Long timeBlockId = 1L;

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공 - 204 No Content")
        void delete_success() throws Exception {
            // given
            willDoNothing().given(timelineService).deleteTimeBlock(userId, timeBlockId);

            // when & then
            mockMvc.perform(delete("/api/v1/timeline/block/{timeBlockId}", timeBlockId).with(csrf()))
                    .andExpect(status().isNoContent());
        }
    }
}