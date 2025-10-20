package com.lifelogix.user.api.dto.response;

public record AccessTokenResponse(String accessToken, String tokenType) {
    public static AccessTokenResponse of(String accessToken) {
        return new AccessTokenResponse(accessToken, "Bearer");
    }
}
