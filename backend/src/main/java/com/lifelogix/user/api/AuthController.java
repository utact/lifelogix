package com.lifelogix.user.api;

import com.lifelogix.user.application.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        userService.register(
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getUsername()
        );

        // API 명세에 따른 성공 응답 생성
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "회원가입이 성공적으로 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        // 1. UserService를 통해 인증 후 JWT 토큰 받기
        String accessToken = userService.login(loginRequest.getEmail(), loginRequest.getPassword());

        // 2. API 명세에 따라 TokenResponse DTO로 감싸서 응답
        TokenResponse tokenResponse = TokenResponse.of(accessToken);

        return ResponseEntity.ok(tokenResponse);
    }
}
