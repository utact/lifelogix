package com.lifelogix.user.api.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
    /**
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @return 새로운 TokenResponse 객체
     **/
    public static TokenResponse of(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken, "Bearer");
    }
}