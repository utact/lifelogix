package com.lifelogix.user.application;

import com.lifelogix.config.jwt.JwtTokenProvider;
import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    /**
     * @Mock: 테스트 대상(UserService)이 의존하는 객체들을 가짜(Mock)로 생성
     * -> 외부 환경(DB, 외부 API 등)으로부터 테스트 격리 가능
     **/
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    /**
     * @InjectMocks: @Mock으로 생성된 가짜 객체들을 실제 테스트 대상(UserService)에 자동으로 주입
     **/
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공")
    void 회원가입이_성공해야_한다() {
        /**
         * 1. Arrange (Given - 준비)
         **/
        String email = "test@example.com";
        String password = "!TestPassword123";
        String username = "tester";

        User fakeUser = User.builder()
                .id(1L)
                .email(email)
                .password("encodedPassword")
                .username(username)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(fakeUser);

        /**
         * 2. Act (When - 실행)
         **/
        User registeredUser = userService.register(email, password, username);

        /**
         * 3. Assert (Then - 검증)
         **/
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getEmail()).isEqualTo(email);
        assertThat(registeredUser.getUsername()).isEqualTo(username);
        assertThat(registeredUser.getId()).isEqualTo(1L);

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
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

    @Test
    @DisplayName("로그인 성공")
    void 올바른_정보로_로그인에_성공해야_한다() {
        /**
         * 1. Arrange (Given - 준비)
         **/
        String email = "test@example.com";
        String rawPassword = "!TestPassword123";
        String encodedPassword = "encodedPassword";
        String fakeToken = "fake.jwt.token";

        User foundUser = User.builder()
                .id(1L)
                .email(email)
                .password(encodedPassword)
                .username("tester")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(foundUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtTokenProvider.generateToken(foundUser)).thenReturn(fakeToken);

        /**
         * 2. Act (When - 실행)
         **/
        String accessToken = userService.login(email, rawPassword);

        /**
         * 3. Assert (Then - 검증)
         **/
        // 반환된 토큰이 예상한 가짜 토큰과 일치하는지 검증
        assertThat(accessToken).isEqualTo(fakeToken);

        // jwtTokenProvider.generateToken() 메서드가 정확히 1번 호출되었는지 검증
        verify(jwtTokenProvider).generateToken(foundUser);
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
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
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
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