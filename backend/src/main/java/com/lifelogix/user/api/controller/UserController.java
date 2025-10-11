package com.lifelogix.user.api.controller;

import com.lifelogix.user.api.dto.response.UserResponse;
import com.lifelogix.user.application.UserService;
import com.lifelogix.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 현재 인증된 사용자의 정보(내 정보)를 조회
     **/
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(UserResponse.from(user));
    }
}