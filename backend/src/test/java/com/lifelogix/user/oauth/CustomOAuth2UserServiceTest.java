package com.lifelogix.user.oauth;

import com.lifelogix.user.PrincipalDetails;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import com.lifelogix.user.domain.ProviderType;
import com.lifelogix.user.domain.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomOAuth2UserService 테스트")
class CustomOAuth2UserServiceTest {

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private UserRepository userRepository;

    private OAuth2UserRequest userRequest;
    private OAuth2User oAuth2User;

    @BeforeEach
    void setUp() {
        userRequest = mock(OAuth2UserRequest.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        Map<String, Object> attributes = Map.of(
                "sub", "123456789",
                "name", "Test User",
                "email", "test@example.com"
        );
        oAuth2User = new DefaultOAuth2User(null, attributes, "sub");

        given(userRequest.getClientRegistration().getRegistrationId()).willReturn("google");
    }

    @Test
    @DisplayName("신규 소셜 로그인 사용자 - 회원가입")
    void loadUser_newUser_shouldCreateUser() {
        // given
        User savedUser = User.builder()
                .id(1L) // Assuming an ID is generated on save
                .email("test@example.com")
                .nickname("Test User")
                .providerType(ProviderType.GOOGLE)
                .providerId("123456789")
                .roleType(RoleType.USER)
                .build();

        given(userRepository.findByProviderTypeAndProviderId(ProviderType.GOOGLE, "123456789")).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(savedUser); // Fix: return a real object

        // when
        CustomOAuth2UserService spyUserService = org.mockito.Mockito.spy(customOAuth2UserService);
        org.mockito.Mockito.doReturn(oAuth2User).when(spyUserService).loadOAuth2User(userRequest);
        PrincipalDetails principalDetails = (PrincipalDetails) spyUserService.loadUser(userRequest);

        // then
        assertThat(principalDetails.getEmail()).isEqualTo("test@example.com");
        assertThat(principalDetails.getNickname()).isEqualTo("Test User");
        assertThat(principalDetails.getRoleType()).isEqualTo(RoleType.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("기존 소셜 로그인 사용자 - 정보 업데이트")
    void loadUser_existingUser_shouldUpdateUser() {
        // given
        User existingUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("Old Name")
                .providerType(ProviderType.GOOGLE)
                .providerId("123456789")
                .roleType(RoleType.USER)
                .build();
        given(userRepository.findByProviderTypeAndProviderId(ProviderType.GOOGLE, "123456789")).willReturn(Optional.of(existingUser));

        // when
        CustomOAuth2UserService spyUserService = org.mockito.Mockito.spy(customOAuth2UserService);
        org.mockito.Mockito.doReturn(oAuth2User).when(spyUserService).loadOAuth2User(userRequest);
        PrincipalDetails principalDetails = (PrincipalDetails) spyUserService.loadUser(userRequest);

        // then
        assertThat(principalDetails.getId()).isEqualTo(1L);
        assertThat(principalDetails.getNickname()).isEqualTo("Test User"); // Nickname should be updated
    }
}
