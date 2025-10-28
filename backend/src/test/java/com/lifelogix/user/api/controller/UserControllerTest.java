package com.lifelogix.user.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelogix.user.WithMockCustomUser;
import com.lifelogix.user.application.UserService;
import com.lifelogix.user.domain.User;
import com.lifelogix.config.TestSecurityConfig;
import com.lifelogix.config.jwt.JwtProperties;
import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({TestSecurityConfig.class, UserControllerTest.TestConfig.class})
@ActiveProfiles("local") // TestConfig Import
@DisplayName("UserController 통합 테스트")
class UserControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtProperties jwtProperties() {
            JwtProperties mockProperties = mock(JwtProperties.class);
            given(mockProperties.getSecret()).willReturn("bGlmZWxvZ2l4LWp3dC1zZWNyZXQta2V5LWZvci10ZXN0LWVudmlyb25tZW50Cg==");
            return mockProperties;
        }
    }

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("내 정보 조회 API")
    class GetMyInfoAPITest {

        @Test
        @WithMockCustomUser
        @DisplayName("성공")
        void getMyInfo_api_success() throws Exception {
            // given
            Long userId = 1L;
            User fakeUser = User.builder()
                    .id(userId)
                    .email("test@example.com")
                    .nickname("tester")
                    .build();

            given(userService.getUserById(userId)).willReturn(fakeUser);

            // when & then
            mockMvc.perform(get("/api/v1/users/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.nickname").value("tester"));
        }

        @Test
        @WithMockCustomUser(id = 999L)
        @DisplayName("실패 - 존재하지 않는 사용자")
        void getMyInfo_api_fail_userNotFound() throws Exception {
            // given
            Long userId = 999L;
            given(userService.getUserById(userId))
                    .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/v1/users/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }


    }
}