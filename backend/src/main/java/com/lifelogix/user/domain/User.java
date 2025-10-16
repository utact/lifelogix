package com.lifelogix.user.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 정보를 나타내는 JPA 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"password", "refreshToken"}) // 로그 출력 시 민감 정보 제외
@EqualsAndHashCode(of = "id")
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 10)
    private String username;

    @Column(unique = true)
    // TODO: [리팩토링] Refresh Token을 Redis와 같은 인메모리 DB로 이전하여 성능 및 보안 강화 예정
    private String refreshToken;

    // @AllArgsConstructor 대신 @Builder를 위한 생성자 명시
    @Builder
    public User(Long id, String email, String password, String username, String refreshToken) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.refreshToken = refreshToken;
    }

    // ADR-011 컨벤션에 따른 테스트용 생성자 추가
    @Deprecated
    public User(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}