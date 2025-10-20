package com.lifelogix.user.application;

import com.lifelogix.config.jwt.JwtProperties;
import com.lifelogix.user.PrincipalDetails;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import com.lifelogix.config.jwt.JwtTokenProvider;
import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.user.api.dto.request.UserLoginRequest;
import com.lifelogix.user.api.dto.request.UserRegisterRequest;
import com.lifelogix.user.api.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    @Transactional
    public void register(UserRegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.username())
                .build();

        userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(UserLoginRequest request) {
        log.info("Logging in user with email: {}", request.email());
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        log.info("User found: {}", user.getEmail());

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Password does not match for user: {}", user.getEmail());
            throw new BusinessException(ErrorCode.USER_PASSWORD_NOT_MATCH);
        }

        log.info("Password matches for user: {}", user.getEmail());

        PrincipalDetails principalDetails = PrincipalDetails.create(user);
        log.info("PrincipalDetails created for user: {}", user.getEmail());

        String accessToken = jwtTokenProvider.generateAccessToken(principalDetails);
        log.info("Access token generated for user: {}", user.getEmail());

        String refreshToken = jwtTokenProvider.generateRefreshToken(principalDetails);
        log.info("Refresh token generated for user: {}", user.getEmail());

        long refreshTokenValiditySeconds = jwtProperties.getRefreshTokenValiditySeconds();
        redisTemplate.opsForValue().set(
                user.getId().toString(),
                refreshToken,
                Duration.ofSeconds(refreshTokenValiditySeconds)
        );
        log.info("Refresh token saved to Redis for user: {}", user.getEmail());

        return TokenResponse.of(accessToken, refreshToken);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void logout(Long userId) {
        redisTemplate.delete(userId.toString());
        log.info("User {} logged out, refresh token deleted from Redis.", userId);
    }

    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String userId = jwtTokenProvider.getClaims(refreshToken).getSubject();
        String storedRefreshToken = redisTemplate.opsForValue().get(userId);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        PrincipalDetails principalDetails = PrincipalDetails.create(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(principalDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(principalDetails);

        long refreshTokenValiditySeconds = jwtProperties.getRefreshTokenValiditySeconds();
        redisTemplate.opsForValue().set(
                userId,
                newRefreshToken,
                Duration.ofSeconds(refreshTokenValiditySeconds)
        );

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }
}