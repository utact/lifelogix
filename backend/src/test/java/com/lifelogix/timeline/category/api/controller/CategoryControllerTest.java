package com.lifelogix.timeline.category.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelogix.config.jwt.JwtTokenProvider;
import com.lifelogix.timeline.category.api.dto.request.CreateCategoryRequest;
import com.lifelogix.timeline.category.api.dto.request.UpdateCategoryRequest;
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
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder().email("test@user.com").password("p").username("tester").build();
        userRepository.save(testUser);
        accessToken = jwtTokenProvider.generateAccessToken(testUser);
    }

    @Nested
    @DisplayName("GET /api/v1/categories")
    class GetCategories {
        @Test
        @DisplayName("성공")
        void 사용자가_조회_가능한_모든_카테고리를_가져온다() throws Exception {
            // given
            categoryRepository.save(new Category("운동", "#2ECC71", null, null));
            categoryRepository.save(new Category("개인 프로젝트", "#E74C3C", testUser, null));
            User otherUser = User.builder().email("other@user.com").password("p").username("other").build();
            userRepository.save(otherUser);
            categoryRepository.save(new Category("다른 유저 카테고리", "#FFFFFF", otherUser, null));

            // when & then
            mockMvc.perform(get("/api/v1/categories")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[?(@.name == '운동')]").exists())
                    .andExpect(jsonPath("$[?(@.name == '개인 프로젝트')]").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/categories")
    class CreateCategory {
        @Test
        @DisplayName("성공")
        void 사용자_정의_카테고리_생성에_성공한다() throws Exception {
            // given
            Category parentCategory = categoryRepository.save(new Category("학습", "#9B59B6", null, null));
            var request = new CreateCategoryRequest("AWS 자격증 공부", "#F39C12", parentCategory.getId());

            // when & then
            mockMvc.perform(post("/api/v1/categories")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("AWS 자격증 공부"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/categories/{categoryId}")
    class UpdateCategory {
        @Test
        @DisplayName("성공")
        void 자신의_카테고리_수정에_성공한다() throws Exception {
            // given
            Category myCategory = categoryRepository.save(new Category("원본 이름", "#111111", testUser, null));
            var request = new UpdateCategoryRequest("수정된 이름", "#222222");

            // when & then
            mockMvc.perform(put("/api/v1/categories/" + myCategory.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("수정된 이름"))
                    .andExpect(jsonPath("$.color").value("#222222"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/categories/{categoryId}")
    class DeleteCategory {
        @Test
        @DisplayName("성공")
        void 자신의_카테고리_삭제에_성공한다() throws Exception {
            // given
            Category myCategory = categoryRepository.save(new Category("삭제될 카테고리", "#111111", testUser, null));

            // when & then
            mockMvc.perform(delete("/api/v1/categories/" + myCategory.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void 다른_사람의_카테고리는_삭제할_수_없다() throws Exception {
            // given
            User otherUser = User.builder().email("other@user.com").password("p").username("other").build();
            userRepository.save(otherUser);
            Category otherCategory = categoryRepository.save(new Category("남의 카테고리", "#FFFFFF", otherUser, null));

            // when & then
            mockMvc.perform(delete("/api/v1/categories/" + otherCategory.getId())
                            .header("Authorization", "Bearer " + accessToken)) // 내 토큰으로 남의 카테고리 삭제 시도
                    .andExpect(status().isForbidden());
        }
    }
}