package com.lifelogix.config.jwt;

import com.lifelogix.user.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-ms}") long expirationMilliseconds
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMilliseconds = expirationMilliseconds;
    }

    /**
     * 사용자 정보를 기반으로 Access Token 생성
     * @param user 인증된 사용자 객체
     * @return 생성된 JWT 문자열
     **/
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMilliseconds);

        return Jwts.builder()
                // 토큰의 주체(subject)로 사용자의 이메일을 설정
                .setSubject(user.getEmail())
                // 토큰 발급 시간을 설정
                .setIssuedAt(now)
                // 토큰 만료 시간을 설정
                .setExpiration(expiryDate)
                // 서명에 사용할 알고리즘과 비밀 키를 설정
                .signWith(key, SignatureAlgorithm.HS256)
                // 최종적으로 JWT를 생성하고 문자열로 직렬화
                .compact();
    }

    // --- TODO: 토큰 검증 및 정보 추출 메서드는 다음 단계에서 구현 ---
}
