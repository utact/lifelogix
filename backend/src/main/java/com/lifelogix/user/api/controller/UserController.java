package com.lifelogix.user.api.controller;

import com.lifelogix.user.PrincipalDetails;
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

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        User user = userService.getUserById(principalDetails.getId());
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
