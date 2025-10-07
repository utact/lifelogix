package com.lifelogix.timeline.category.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelogix.config.jwt.JwtTokenProvider;
import com.lifelogix.timeline.category.api.dto.request.CreateCategoryRequest;
import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.timeline.category.domain.CategoryRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 각 테스트 후 롤백하여 테스트 격리 보장
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
        // 테스트용 사용자 생성 및 저장
        testUser = User.builder().email("test@user.com").password("p").username("tester").build();
        userRepository.save(testUser);

        // 테스트용 JWT 토큰 발급
        accessToken = jwtTokenProvider.generateToken(testUser);
    }

    @Test
    @DisplayName("카테고리 생성 API")
    void 사용자_정의_카테고리_생성에_성공한다() throws Exception {
        // given
        // 시스템 기본 카테고리 미리 저장
        Category parentCategory = categoryRepository.save(new Category("학습", "#9B59B6", null, null));
        var request = new CreateCategoryRequest("AWS 자격증 공부", "#F39C12", parentCategory.getId());

        // when & then
        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("AWS 자격증 공부"))
                .andExpect(jsonPath("$.isCustom").value(true))
                .andExpect(jsonPath("$.parentId").value(parentCategory.getId()));
    }

    @Test
    @DisplayName("카테고리 조회 API")
    void 사용자가_조회_가능한_모든_카테고리를_가져온다() throws Exception {
        // given
        // 1. 시스템 카테고리 저장
        categoryRepository.save(new Category("운동", "#2ECC71", null, null));
        // 2. 현재 테스트 사용자의 커스텀 카테고리 저장
        categoryRepository.save(new Category("개인 프로젝트", "#E74C3C", testUser, null));
        // 3. 다른 사용자의 커스텀 카테고리 저장 (조회 불가능 기대)
        User otherUser = User.builder().email("other@user.com").password("p").username("other").build();
        userRepository.save(otherUser);
        categoryRepository.save(new Category("다른 유저 카테고리", "#FFFFFF", otherUser, null));

        // when & then
        mockMvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // 시스템 카테고리 + 내 카테고리 = 2개
                .andExpect(jsonPath("$[?(@.name == '운동')]").exists())
                .andExpect(jsonPath("$[?(@.name == '개인 프로젝트')]").exists());
    }
}