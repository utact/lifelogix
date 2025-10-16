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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtDecoder jwtDecoder;

    // 공통 사용 변수
    private final String email = "test@example.com";
    private final String password = "password123";
    private final String username = "tester";
    private final Long userId = 1L;

    @Nested
    @DisplayName("회원가입 기능")
    class RegisterTest {

        @Test
        @DisplayName("성공")
        void register_success() {
            // given
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());
            given(passwordEncoder.encode(password)).willReturn("encodedPassword");

            // when
            userService.register(email, password, username);

            // then
            then(userRepository).should(times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("실패 - 이미 존재하는 이메일")
        void register_fail_emailAlreadyExists() {
            // given
            given(userRepository.findByEmail(email)).willReturn(Optional.of(mock(User.class)));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.register(email, password, username));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("로그인 기능")
    class LoginTest {
        @Test
        @DisplayName("성공")
        void login_success() {
            // given
            User user = User.builder().password("encodedPassword").build();
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(password, "encodedPassword")).willReturn(true);
            given(jwtTokenProvider.generateAccessToken(user)).willReturn("access-token");
            given(jwtTokenProvider.generateRefreshToken(user)).willReturn("refresh-token");

            // when
            TokenResponse tokenResponse = userService.login(email, password);

            // then
            assertThat(tokenResponse.accessToken()).isEqualTo("access-token");
            assertThat(tokenResponse.refreshToken()).isEqualTo("refresh-token");
            assertThat(tokenResponse.tokenType()).isEqualTo("Bearer");
            assertThat(user.getRefreshToken()).isEqualTo("refresh-token");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void login_fail_userNotFound() {
            // given
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.login(email, password));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void login_fail_passwordMismatch() {
            // given
            User user = User.builder().password("encodedPassword").build();
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(password, "encodedPassword")).willReturn(false);

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.login(email, password));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
        }
    }

    @Nested
    @DisplayName("액세스 토큰 갱신 기능")
    class RefreshAccessTokenTest {

        @Test
        @DisplayName("성공")
        void refresh_success() {
            // given
            String refreshToken = "valid-refresh-token";
            User user = User.builder().id(userId).build();
            Jwt mockJwt = mock(Jwt.class);

            given(jwtDecoder.decode(refreshToken)).willReturn(mockJwt); // 토큰 디코딩 성공
            given(userRepository.findByRefreshToken(refreshToken)).willReturn(Optional.of(user));
            given(jwtTokenProvider.generateAccessToken(user)).willReturn("new-access-token");

            // when
            String newAccessToken = userService.refreshAccessToken(refreshToken);

            // then
            assertThat(newAccessToken).isEqualTo("new-access-token");
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 토큰 (디코딩 실패)")
        void refresh_fail_invalidToken() {
            // given
            String refreshToken = "invalid-refresh-token";
            given(jwtDecoder.decode(refreshToken)).willThrow(new JwtException("Invalid token"));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.refreshAccessToken(refreshToken));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TOKEN_INVALID);
        }

        @Test
        @DisplayName("실패 - DB에 존재하지 않는 토큰")
        void refresh_fail_tokenNotFoundInDb() {
            // given
            String refreshToken = "valid-but-not-in-db-token";
            Jwt mockJwt = mock(Jwt.class);

            given(jwtDecoder.decode(refreshToken)).willReturn(mockJwt); // 디코딩은 성공
            given(userRepository.findByRefreshToken(refreshToken)).willReturn(Optional.empty()); // DB에는 부재

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.refreshAccessToken(refreshToken));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TOKEN_INVALID);
        }
    }

    @Nested
    @DisplayName("로그아웃 기능")
    class LogoutTest {
        @Test
        @DisplayName("성공")
        void logout_success() {
            // given
            User user = User.builder().id(userId).refreshToken("some-refresh-token").build();
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            userService.logout(userId);

            // then
            assertThat(user.getRefreshToken()).isNull();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void logout_fail_userNotFound() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.logout(userId));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("사용자 ID로 조회 기능")
    class GetUserByIdTest {

        @Test
        @DisplayName("성공")
        void getUser_success() {
            // given
            User expectedUser = User.builder().id(userId).email(email).build();
            given(userRepository.findById(userId)).willReturn(Optional.of(expectedUser));

            // when
            User actualUser = userService.getUserById(userId);

            // then
            assertThat(actualUser).isEqualTo(expectedUser);
            assertThat(actualUser.getId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void getUser_fail_userNotFound() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.getUserById(userId));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }
}