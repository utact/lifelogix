package com.lifelogix.user.api.controller;

import com.lifelogix.config.jwt.JwtProperties;
import com.lifelogix.config.jwt.JwtTokenProvider;
import com.lifelogix.user.PrincipalDetails;
import com.lifelogix.user.api.dto.request.OAuthTokenRequest;
import com.lifelogix.user.api.dto.request.UserLoginRequest;
import com.lifelogix.user.api.dto.request.UserRegisterRequest;
import com.lifelogix.user.api.dto.response.AccessTokenResponse;
import com.lifelogix.user.api.dto.response.TokenResponse;
import com.lifelogix.user.application.UserService;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import com.lifelogix.user.oauth.OAuthTempCodeService;
import com.lifelogix.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final JwtProperties jwtProperties;
    private final OAuthTempCodeService oAuthTempCodeService;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("[Backend|AuthController] Register - Received request for email: {}", request.email());
        userService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "회원가입이 성공적으로 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody UserLoginRequest request, HttpServletResponse response) {
        log.info("[Backend|AuthController] Login - Received request for email: {}", request.email());
        TokenResponse tokenResponse = userService.login(request);

        CookieUtil.addCookie(
                response,
                "refresh_token",
                tokenResponse.refreshToken(),
                (int) jwtProperties.getRefreshTokenValiditySeconds()
        );

        return ResponseEntity.ok(AccessTokenResponse.of(tokenResponse.accessToken()));
    }

    @PostMapping("/oauth-token")
    public ResponseEntity<AccessTokenResponse> exchangeCodeForToken(@RequestBody OAuthTokenRequest tokenRequest, HttpServletResponse response) {
        log.info("[Backend|AuthController] OAuthToken - Received request with code");
        Long userId = oAuthTempCodeService.exchange(tokenRequest.getCode());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        PrincipalDetails principalDetails = PrincipalDetails.create(user);

        String accessToken = tokenProvider.generateAccessToken(principalDetails);
        String refreshToken = tokenProvider.generateRefreshToken(principalDetails);

        long refreshTokenValiditySeconds = jwtProperties.getRefreshTokenValiditySeconds();

        redisTemplate.opsForValue().set(
                userId.toString(),
                refreshToken,
                Duration.ofSeconds(refreshTokenValiditySeconds)
        );

        CookieUtil.addCookie(response, "refresh_token", refreshToken, (int) refreshTokenValiditySeconds);

        log.info("[Backend|AuthController] OAuthToken - Successfully issued tokens for userId: {}", userId);
        return ResponseEntity.ok(AccessTokenResponse.of(accessToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refreshAccessToken(
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletResponse response) {
        log.info("[Backend|AuthController] RefreshAccessToken - Received request");
        TokenResponse newTokens = userService.refreshAccessToken(refreshToken);

        CookieUtil.addCookie(
                response,
                "refresh_token",
                newTokens.refreshToken(),
                (int) jwtProperties.getRefreshTokenValiditySeconds()
        );

        return ResponseEntity.ok(AccessTokenResponse.of(newTokens.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId, HttpServletRequest request, HttpServletResponse response) {
        log.info("[Backend|AuthController] Logout - Received request for userId: {}", userId);
        userService.logout(userId);
        CookieUtil.deleteCookie(request, response, "refresh_token");
        return ResponseEntity.ok().build();
    }
}