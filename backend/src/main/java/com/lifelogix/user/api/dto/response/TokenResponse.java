package com.lifelogix.user.api.dto.response;

public record TokenResponse(
        String accessToken,
        String tokenType
) {
    public static TokenResponse of(String accessToken) {
        return new TokenResponse(accessToken, "Bearer");
    }
}
