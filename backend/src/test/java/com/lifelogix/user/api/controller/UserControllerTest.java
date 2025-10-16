package com.lifelogix.user.api.controller;

import com.lifelogix.config.SecurityConfig;
import com.lifelogix.config.jwt.JwtProperties;
import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.user.application.UserService;
import com.lifelogix.user.domain.User;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, UserControllerTest.TestConfig.class})
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

    @Nested
    @DisplayName("내 정보 조회 API")
    class GetMyInfoAPITest {

        @Test
        @WithMockUser(username = "1") // 인증된 사용자로 '1'을 사용
        @DisplayName("성공")
        void getMyInfo_api_success() throws Exception {
            // given
            Long userId = 1L;
            User fakeUser = User.builder()
                    .id(userId)
                    .email("test@example.com")
                    .username("tester")
                    .build();

            given(userService.getUserById(userId)).willReturn(fakeUser);

            // when & then
            mockMvc.perform(get("/api/v1/users/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.username").value("tester"));
        }

        @Test
        @WithMockUser(username = "999") // 존재하지 않는 사용자로 가정
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

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자")
        void getMyInfo_api_fail_unauthorized() throws Exception {
            // given
            // @WithMockUser 없음 (인증 정보가 없는 상태)

            // when & then
            mockMvc.perform(get("/api/v1/users/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized()); // 401 Unauthorized
        }
    }
}