package com.lifelogix.user.api.dto.response;

import com.lifelogix.user.domain.User;

public record UserResponse(
        Long id,
        String email,
        String username
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getUsername());
    }
}