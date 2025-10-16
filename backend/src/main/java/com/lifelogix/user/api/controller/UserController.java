package com.lifelogix.user.api.controller;

import com.lifelogix.user.api.dto.response.UserResponse;
import com.lifelogix.user.application.UserService;
import com.lifelogix.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    /**
     * 현재 인증된 사용자의 정보(내 정보)를 조회
     **/
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        log.info("[Backend|UserController] GetMyInfo - Received request from userId: {}", userId);
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(UserResponse.from(user));
    }
}