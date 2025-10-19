package com.lifelogix.user.api.dto.response;

import com.lifelogix.user.domain.User;
import lombok.Builder;

@Builder
public record UserResponse(Long id, String email, String nickname) {

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
