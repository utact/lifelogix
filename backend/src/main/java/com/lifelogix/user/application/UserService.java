package com.lifelogix.user.application;

import com.lifelogix.config.jwt.JwtTokenProvider;
import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.user.api.dto.response.TokenResponse;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void register(String email, String password, String username) {
        log.info("[Backend|UserService] Register - Attempt for email: {}", email);
        userRepository.findByEmail(email).ifPresent(user -> {
            log.warn("[Backend|UserService] Register - Failed: Email already exists for {}", email);
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        });
        String encodedPassword = passwordEncoder.encode(password);
        User newUser = User.builder()
                .email(email)
                .password(encodedPassword)
                .username(username)
                .build();
        userRepository.save(newUser);
        log.info("[Backend|UserService] Register - Success for email: {}", email);
    }

    @Transactional
    public TokenResponse login(String email, String password) {
        log.info("[Backend|UserService] Login - Attempt for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[Backend|UserService] Login - Failed: User not found for email: {}", email);
                    return new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("[Backend|UserService] Login - Failed: Password mismatch for user: {}", email);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        user.updateRefreshToken(refreshToken);
        log.info("[Backend|UserService] Login - Success, tokens generated for user: {}", email);
        return TokenResponse.of(accessToken, refreshToken);
    }

    @Transactional
    public String refreshAccessToken(String refreshToken) {
        log.info("[Backend|UserService] RefreshAccessToken - Attempt");
        jwtTokenProvider.validateToken(refreshToken);

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("[Backend|UserService] RefreshAccessToken - Failed: Invalid refresh token");
                    return new BusinessException(ErrorCode.TOKEN_INVALID);
                });

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        log.info("[Backend|UserService] RefreshAccessToken - Success for user: {}", user.getEmail());
        return newAccessToken;
    }

    @Transactional
    public void logout(Long userId) {
        log.info("[Backend|UserService] Logout - Attempt for userId: {}", userId);
        User user = getUserById(userId);
        user.updateRefreshToken(null);
        log.info("[Backend|UserService] Logout - Success for userId: {}", userId);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[Backend|UserService] GetUserById - Failed: User not found for id: {}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });
    }
}