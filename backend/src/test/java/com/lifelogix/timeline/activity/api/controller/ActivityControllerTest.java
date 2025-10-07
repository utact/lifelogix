package com.lifelogix.timeline.activity.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelogix.config.jwt.JwtTokenProvider;
import com.lifelogix.timeline.activity.api.dto.request.CreateActivityRequest;
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
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String accessToken;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder().email("activity@test.com").password("p").username("activity user").build();
        userRepository.save(testUser);

        accessToken = jwtTokenProvider.generateToken(testUser);

        testCategory = new Category("업무", "#123456", testUser, null);
        categoryRepository.save(testCategory);
    }

    @Test
    @DisplayName("활동 생성 API")
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
    @DisplayName("활동 조회 API")
    void 카테고리별로_그룹화된_활동_목록을_조회한다() throws Exception {
        // given
        // API 호출이 정상적으로 200 OK를 반환하는지만 확인

        // when & then
        mockMvc.perform(get("/api/v1/activities")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("활동 생성 실패 API - 존재하지 않는 카테고리")
    void 존재하지_않는_카테고리로는_활동을_생성할_수_없다() throws Exception {
        // given
        long nonExistentCategoryId = 999L;
        var request = new CreateActivityRequest("실패할 활동", nonExistentCategoryId);

        // when & then
        mockMvc.perform(post("/api/v1/activities")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}