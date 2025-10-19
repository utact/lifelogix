package com.lifelogix.user.oauth.user;

import com.lifelogix.user.domain.ProviderType;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(ProviderType providerType, Map<String, Object> attributes) {
        switch (providerType) {
            case GOOGLE:
                return new GoogleOAuth2UserInfo(attributes);
            case GITHUB:
                return new GithubOAuth2UserInfo(attributes);
            default:
                throw new IllegalArgumentException("Invalid Provider Type.");
        }
    }
}
