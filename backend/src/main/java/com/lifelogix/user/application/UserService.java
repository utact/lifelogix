package com.lifelogix.user.application;

import com.lifelogix.config.jwt.JwtTokenProvider;
import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.user.api.dto.response.TokenResponse;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void register(String email, String password, String username) { // 반환 타입을 void로 변경 -> 캡슐화 강화
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        });
        String encodedPassword = passwordEncoder.encode(password);
        User newUser = User.builder()
                .email(email)
                .password(encodedPassword)
                .username(username)
                .build();
        userRepository.save(newUser);
    }

    @Transactional
    public TokenResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTHENTICATION_FAILED));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        user.updateRefreshToken(refreshToken);

        return TokenResponse.of(accessToken, refreshToken); // of() 정적 팩토리 메서드 사용으로 변경
    }

    @Transactional
    public String refreshAccessToken(String refreshToken) {
        jwtTokenProvider.validateToken(refreshToken);

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

        return jwtTokenProvider.generateAccessToken(user);
    }

    @Transactional
    public void logout(Long userId) {
        User user = getUserById(userId);
        user.updateRefreshToken(null);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}