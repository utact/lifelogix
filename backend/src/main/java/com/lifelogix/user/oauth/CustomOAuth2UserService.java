package com.lifelogix.user.oauth;

import com.lifelogix.user.PrincipalDetails;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import com.lifelogix.user.oauth.user.OAuth2UserInfo;
import com.lifelogix.user.oauth.user.OAuth2UserInfoFactory;
import com.lifelogix.user.domain.ProviderType;
import com.lifelogix.user.domain.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("Attempting to load user from OAuth2 request");

        OAuth2User oAuth2User = loadOAuth2User(userRequest);

        ProviderType providerType = ProviderType.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, oAuth2User.getAttributes());

        Optional<User> userOptional = userRepository.findByProviderTypeAndProviderId(providerType, oAuth2UserInfo.getId());

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            log.info("User found in database: {}", user.getEmail());
            updateUser(user, oAuth2UserInfo);
        } else {
            log.info("User not found, creating new user");
            user = createUser(oAuth2UserInfo, providerType);
        }

        return PrincipalDetails.create(user, oAuth2User.getAttributes());
    }

    protected OAuth2User loadOAuth2User(OAuth2UserRequest userRequest) {
        return super.loadUser(userRequest);
    }

    private User createUser(OAuth2UserInfo userInfo, ProviderType providerType) {
        User user = User.builder()
                .email(userInfo.getEmail())
                .nickname(userInfo.getName())
                .providerType(providerType)
                .providerId(userInfo.getId())
                .roleType(RoleType.USER)
                .build();
        return userRepository.save(user);
    }

    private void updateUser(User user, OAuth2UserInfo userInfo) {
        if (userInfo.getName() != null && !user.getNickname().equals(userInfo.getName())) {
            user.updateNickname(userInfo.getName());
        }
    }
}
