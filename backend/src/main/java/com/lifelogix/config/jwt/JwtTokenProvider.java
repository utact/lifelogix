package com.lifelogix.config.jwt;

import com.lifelogix.user.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT(Access Token)의 생성을 담당하는 Provider 클래스
 * 토큰의 검증 및 파싱 관련 로직은 Spring Security의 JwtDecoder에 위임
 **/
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-ms}") long expirationMilliseconds
    ) {
        // application.yml의 secret key는 Base64로 인코딩된 값이므로, 디코딩하여 SecretKey 객체를 생성
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMilliseconds = expirationMilliseconds;
    }

    /**
     * 사용자 정보를 기반으로 Access Token을 생성
     * @param user 인증된 사용자 객체
     * @return 생성된 JWT 문자열
     **/
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMilliseconds);

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key) // Key를 통해 알고리즘 자동 선택 (HS256 or higher)
                .compact();
    }
}
