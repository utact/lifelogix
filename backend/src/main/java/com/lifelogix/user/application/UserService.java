package com.lifelogix.user.application;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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

        return TokenResponse.of(accessToken, refreshToken);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}