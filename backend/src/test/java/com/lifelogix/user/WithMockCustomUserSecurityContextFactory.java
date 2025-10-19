package com.lifelogix.user;

import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.ProviderType;
import com.lifelogix.user.domain.RoleType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User user = User.builder()
                .id(annotation.id())
                .email(annotation.email())
                .nickname(annotation.nickname())
                .providerType(ProviderType.GOOGLE)
                .roleType(RoleType.USER)
                .build();

        PrincipalDetails principalDetails = PrincipalDetails.create(user);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                principalDetails,
                null,
                principalDetails.getAuthorities()
        );

        context.setAuthentication(token);
        return context;
    }
}
