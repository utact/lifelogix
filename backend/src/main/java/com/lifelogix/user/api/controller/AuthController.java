package com.lifelogix.user.api.controller;

import com.lifelogix.user.api.dto.request.LoginRequest;
import com.lifelogix.user.api.dto.request.RegisterRequest;
import com.lifelogix.user.api.dto.response.TokenResponse;
import com.lifelogix.user.application.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("[Backend|AuthController] Register - Received request for email: {}", request.email());
        userService.register(request.email(), request.password(), request.username());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "회원가입이 성공적으로 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("[Backend|AuthController] Login - Received request for email: {}", request.email());
        TokenResponse tokenResponse = userService.login(request.email(), request.password());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshAccessToken(@RequestBody Map<String, String> request) {
        log.info("[Backend|AuthController] RefreshAccessToken - Received request");
        String refreshToken = request.get("refreshToken");
        String newAccessToken = userService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(TokenResponse.of(newAccessToken, refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId) {
        log.info("[Backend|AuthController] Logout - Received request for userId: {}", userId);
        userService.logout(userId);
        return ResponseEntity.ok().build();
    }
}