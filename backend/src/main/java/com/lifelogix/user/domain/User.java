package com.lifelogix.user.domain;

import com.lifelogix.user.domain.ProviderType;
import com.lifelogix.user.domain.RoleType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType providerType;

    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType roleType;

    @Builder
    public User(Long id, String email, String password, String nickname, ProviderType providerType, String providerId, RoleType roleType) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.providerType = providerType;
        this.providerId = providerId;
        this.roleType = roleType;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
