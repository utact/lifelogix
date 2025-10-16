package com.lifelogix.config.jwt;

import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMilliseconds;
    private final long refreshTokenExpirationMilliseconds;

    public JwtTokenProvider(JwtProperties jwtProperties) { // @ConfigurationProperties 사용
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = new SecretKeySpec(keyBytes, "HmacSHA256");
        this.accessTokenExpirationMilliseconds = jwtProperties.getAccessTokenExpirationMs();
        this.refreshTokenExpirationMilliseconds = jwtProperties.getRefreshTokenExpirationMs();
    }

    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpirationMilliseconds);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpirationMilliseconds);
    }

    private String generateToken(User user, long expirationMilliseconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMilliseconds);

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
}