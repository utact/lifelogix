package com.lifelogix.user.application;

import com.lifelogix.user.api.dto.request.UserLoginRequest;
import com.lifelogix.user.api.dto.request.UserRegisterRequest;
import com.lifelogix.user.api.dto.response.TokenResponse;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import com.lifelogix.config.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // This test is for the old login/register functionality and is obsolete.
    // @Test
    // @DisplayName("로그인 성공")
    // void login_success() {
    //     // given
    //     String email = "test@example.com";
    //     String password = "password123!";
    //     String username = "tester";
    //     UserRegisterRequest registerRequest = new UserRegisterRequest(email, password, username);
    //     userService.register(registerRequest);

    //     // when
    //     UserLoginRequest loginRequest = new UserLoginRequest(email, password);
    //     TokenResponse tokenResponse = userService.login(loginRequest);

    //     // then
    //     assertThat(tokenResponse).isNotNull();
    //     assertThat(tokenResponse.accessToken()).isNotBlank();
    //     assertThat(tokenResponse.refreshToken()).isNotBlank();
    // }
}