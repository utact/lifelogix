package com.lifelogix.user.application;

import com.lifelogix.config.jwt.JwtTokenProvider;
import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.user.api.dto.response.TokenResponse;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("회원가입")
    class RegisterTest {

        @Test
        @DisplayName("성공")
        void 회원가입이_성공해야_한다() {
            // given
            String email = "test@example.com";
            String password = "!TestPassword123";
            String username = "tester";

            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

            // when
            userService.register(email, password, username);

            // then
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("실패 - 이메일 중복")
        void 중복된_이메일로는_회원가입할_수_없다() {
            // given
            String email = "duplicate@example.com";
            User existingUser = User.builder().id(1L).email(email).build();
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

            // when & then
            assertThatThrownBy(() -> userService.register(email, "password", "user"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("로그인")
    class LoginTest {

        @Test
        @DisplayName("성공")
        void 올바른_정보로_로그인에_성공해야_한다() {
            // given
            String email = "test@example.com";
            String rawPassword = "!TestPassword123";
            String encodedPassword = "encodedPassword";
            String fakeAccessToken = "fake.access.token";
            String fakeRefreshToken = "fake.refresh.token";

            User foundUser = mock(User.class);
            when(foundUser.getId()).thenReturn(1L);
            when(foundUser.getEmail()).thenReturn(email);
            when(foundUser.getPassword()).thenReturn(encodedPassword);

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(foundUser));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(foundUser)).thenReturn(fakeAccessToken);
            when(jwtTokenProvider.generateRefreshToken(foundUser)).thenReturn(fakeRefreshToken);

            // when
            TokenResponse tokenResponse = userService.login(email, rawPassword);

            // then
            assertThat(tokenResponse.accessToken()).isEqualTo(fakeAccessToken);
            assertThat(tokenResponse.refreshToken()).isEqualTo(fakeRefreshToken);
            verify(foundUser).updateRefreshToken(fakeRefreshToken);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void 존재하지_않는_사용자로는_로그인할_수_없다() {
            // given
            String email = "notfound@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.login(email, "password"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
        }

        @Test
        @DisplayName("실패 - 잘못된 비밀번호")
        void 잘못된_비밀번호로는_로그인할_수_없다() {
            // given
            String email = "test@example.com";
            String wrongPassword = "wrongPassword";
            String encodedPassword = "encodedPassword";
            User foundUser = User.builder().id(1L).email(email).password(encodedPassword).build();

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(foundUser));
            when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.login(email, wrongPassword))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
        }
    }

    @Nested
    @DisplayName("사용자 정보 조회")
    class GetUserByIdTest {

        @Test
        @DisplayName("성공")
        void ID로_사용자_정보를_조회한다() {
            // given
            Long userId = 1L;
            User fakeUser = User.builder().id(userId).email("test@test.com").username("tester").build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser));

            // when
            User foundUser = userService.getUserById(userId);

            // then
            assertThat(foundUser.getId()).isEqualTo(userId);
            assertThat(foundUser.getEmail()).isEqualTo("test@test.com");
        }
    }
}