package com.lifelogix.user;

import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class PrincipalDetails implements UserDetails, OAuth2User {

    private final Long id;
    private final String email;
    private final String password;
    private final String nickname;
    private final RoleType roleType;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    @Override
    public String getName() {
        return nickname;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static PrincipalDetails create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRoleType().getCode()));
        return new PrincipalDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getNickname(),
                user.getRoleType(),
                authorities
        );
    }

    public static PrincipalDetails create(User user, Map<String, Object> attributes) {
        PrincipalDetails principalDetails = create(user);
        principalDetails.setAttributes(attributes);
        return principalDetails;
    }

    private void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}