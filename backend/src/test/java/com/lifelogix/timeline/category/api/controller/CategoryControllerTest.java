package com.lifelogix.timeline.category.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelogix.config.SecurityConfig;
import com.lifelogix.config.jwt.JwtProperties;
import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.timeline.category.api.dto.request.CreateCategoryRequest;
import com.lifelogix.timeline.category.api.dto.request.UpdateCategoryRequest;
import com.lifelogix.timeline.category.api.dto.response.CategoryResponse;
import com.lifelogix.timeline.category.application.CategoryService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, CategoryControllerTest.TestConfig.class})
@DisplayName("CategoryController 통합 테스트")
class CategoryControllerTest {

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
    private CategoryService categoryService;

    private final Long userId = 1L;

    @Nested
    @DisplayName("POST /api/v1/categories - 카테고리 생성")
    class CreateCategory {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공 - 201 Created 반환")
        void create_success() throws Exception {
            // given
            CreateCategoryRequest request = new CreateCategoryRequest("헬스", "#FFFFFF", 10L);
            CategoryResponse response = new CategoryResponse(1L, "헬스", "#FFFFFF", true, 10L);
            // Argument Matcher를 사용하여 Mock 설정
            given(categoryService.createCustomCategory(eq(userId), any(CreateCategoryRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/categories")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/v1/categories/" + response.id()))
                    .andExpect(jsonPath("$.name").value("헬스"));
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자")
        void create_fail_unauthorized() throws Exception {
            // given
            CreateCategoryRequest request = new CreateCategoryRequest("헬스", "#FFFFFF", 10L);

            // when & then
            mockMvc.perform(post("/api/v1/categories")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패 - 이름 중복으로 409 Conflict 반환")
        void create_fail_duplicateName() throws Exception {
            // given
            CreateCategoryRequest request = new CreateCategoryRequest("헬스", "#FFFFFF", 10L);
            given(categoryService.createCustomCategory(eq(userId), any(CreateCategoryRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE));

            // when & then
            mockMvc.perform(post("/api/v1/categories")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories - 모든 카테고리 조회")
    class GetAllCategories {
        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공 - 200 OK와 카테고리 목록 반환")
        void getAll_success() throws Exception {
            // given
            List<CategoryResponse> responses = List.of(
                    new CategoryResponse(10L, "운동", "#000000", false, null),
                    new CategoryResponse(1L, "헬스", "#FFFFFF", true, 10L)
            );
            given(categoryService.findAllCategoriesForUser(userId)).willReturn(responses);

            // when & then
            mockMvc.perform(get("/api/v1/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("운동"))
                    .andExpect(jsonPath("$[1].name").value("헬스"));
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자")
        void getAll_fail_unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/v1/categories"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/categories/{categoryId} - 카테고리 수정")
    class UpdateCategory {
        private final Long categoryId = 1L;

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공 - 200 OK와 수정된 카테고리 반환")
        void update_success() throws Exception {
            // given
            UpdateCategoryRequest request = new UpdateCategoryRequest("필라테스", "#BBBBBB");
            CategoryResponse response = new CategoryResponse(categoryId, "필라테스", "#BBBBBB", true, 10L);
            given(categoryService.updateCustomCategory(eq(userId), eq(categoryId), any(UpdateCategoryRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(put("/api/v1/categories/{categoryId}", categoryId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("필라테스"));
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패 - 권한 없음으로 403 Forbidden 반환")
        void update_fail_permissionDenied() throws Exception {
            // given
            UpdateCategoryRequest request = new UpdateCategoryRequest("필라테스", "#BBBBBB");
            given(categoryService.updateCustomCategory(eq(userId), eq(categoryId), any(UpdateCategoryRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.PERMISSION_DENIED));

            // when & then
            mockMvc.perform(put("/api/v1/categories/{categoryId}", categoryId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/categories/{categoryId} - 카테고리 삭제")
    class DeleteCategory {
        private final Long categoryId = 1L;

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공 - 204 No Content 반환")
        void delete_success() throws Exception {
            // given
            willDoNothing().given(categoryService).deleteCustomCategory(userId, categoryId);

            // when & then
            mockMvc.perform(delete("/api/v1/categories/{categoryId}", categoryId)
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패 - 사용 중인 카테고리로 400 Bad Request 반환")
        void delete_fail_categoryInUse() throws Exception {
            // given
            doThrow(new BusinessException(ErrorCode.CATEGORY_IN_USE))
                    .when(categoryService).deleteCustomCategory(userId, categoryId);

            // when & then
            mockMvc.perform(delete("/api/v1/categories/{categoryId}", categoryId)
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }
}