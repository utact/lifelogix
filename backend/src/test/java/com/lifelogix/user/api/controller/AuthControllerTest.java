package com.lifelogix.user.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelogix.user.api.dto.request.LoginRequest;
import com.lifelogix.user.api.dto.request.RegisterRequest;
import com.lifelogix.user.api.dto.response.TokenResponse;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 API")
    void 회원가입에_성공한다() throws Exception {
        // given
        var request = new RegisterRequest("register@test.com", "!Password123", "register");

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("로그인 API")
    void 로그인에_성공하고_토큰을_발급받는다() throws Exception {
        // given
        userRepository.save(User.builder()
                .email("login@test.com")
                .password(passwordEncoder.encode("!Password123"))
                .username("loginuser")
                .build());

        var request = new LoginRequest("login@test.com", "!Password123");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("토큰 재발급 API")
    void 유효한_리프레시_토큰으로_액세스_토큰을_재발급받는다() throws Exception {
        // given: 먼저 로그인을 해서 유효한 리프레시 토큰을 DB에 저장하고 얻어온다.
        User user = User.builder()
                .email("refresh@test.com")
                .password(passwordEncoder.encode("!Password123"))
                .username("refreshuser")
                .build();
        userRepository.save(user);

        var loginRequest = new LoginRequest("refresh@test.com", "!Password123");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        TokenResponse tokenResponse = objectMapper.readValue(responseBody, TokenResponse.class);
        String refreshToken = tokenResponse.refreshToken();

        // when & then
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").value(refreshToken)) // 기존 리프레시 토큰이 그대로 반환됨
                .andReturn();

        // 새로 발급된 accessToken이 기존 accessToken과 다른지 검증
        String newResponseBody = refreshResult.getResponse().getContentAsString();
        TokenResponse newtokenResponse = objectMapper.readValue(newResponseBody, TokenResponse.class);
        assertThat(newtokenResponse.accessToken()).isNotEqualTo(tokenResponse.accessToken());
    }
}