package com.lifelogix.user.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelogix.config.SecurityConfig;
import com.lifelogix.config.jwt.JwtProperties;
import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.user.api.dto.request.LoginRequest;
import com.lifelogix.user.api.dto.request.RegisterRequest;
import com.lifelogix.user.api.dto.response.TokenResponse;
import com.lifelogix.user.application.UserService;
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

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuthControllerTest.TestConfig.class})
@DisplayName("AuthController 통합 테스트")
class AuthControllerTest {

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
    private UserService userService;

    @Nested
    @DisplayName("회원가입 API")
    class RegisterAPITest {
        @Test
        @DisplayName("성공")
        void register_api_success() throws Exception {
            // given
            RegisterRequest request = new RegisterRequest("test@example.com", "password123!", "tester");
            willDoNothing().given(userService).register(request.email(), request.password(), request.username());

            // when & then
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("회원가입이 성공적으로 완료되었습니다."));
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 비밀번호")
        void register_api_fail_invalidPassword() throws Exception {
            // given
            RegisterRequest request = new RegisterRequest("test@example.com", "password123", "tester");

            // when & then
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다."));
        }
    }

    @Nested
    @DisplayName("로그인 API")
    class LoginAPITest {
        @Test
        @DisplayName("성공")
        void login_api_success() throws Exception {
            // given
            LoginRequest request = new LoginRequest("test@example.com", "password123!");
            TokenResponse tokenResponse = TokenResponse.of("access-token", "refresh-token");
            given(userService.login(request.email(), request.password())).willReturn(tokenResponse);

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("실패 - 잘못된 인증 정보")
        void login_api_fail_authenticationFailed() throws Exception {
            // given
            LoginRequest request = new LoginRequest("test@example.com", "wrong-password");
            given(userService.login(request.email(), request.password()))
                    .willThrow(new BusinessException(ErrorCode.AUTHENTICATION_FAILED));

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized()); // 401 Unauthorized
        }
    }

    @Nested
    @DisplayName("로그아웃 API")
    class LogoutAPITest {
        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공")
        void logout_api_success() throws Exception {
            // given
            willDoNothing().given(userService).logout(1L);

            // when & then
            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("토큰 갱신 API")
    class RefreshTokenAPITest {
        @Test
        @DisplayName("성공")
        void refresh_api_success() throws Exception {
            // given
            String refreshToken = "valid-refresh-token";
            String newAccessToken = "new-access-token";
            given(userService.refreshAccessToken(refreshToken)).willReturn(newAccessToken);

            Map<String, String> requestBody = Map.of("refreshToken", refreshToken);

            // when & then
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(newAccessToken));
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 토큰")
        void refresh_api_fail_invalidToken() throws Exception {
            // given
            String refreshToken = "invalid-refresh-token";
            given(userService.refreshAccessToken(refreshToken))
                    .willThrow(new BusinessException(ErrorCode.TOKEN_INVALID));

            Map<String, String> requestBody = Map.of("refreshToken", refreshToken);

            // when & then
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isUnauthorized());
        }
    }
}