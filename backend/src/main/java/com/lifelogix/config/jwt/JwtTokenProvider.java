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
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMilliseconds;
    private final long refreshTokenExpirationMilliseconds;

    public JwtTokenProvider(JwtProperties jwtProperties) { // @ConfigurationProperties 사용
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
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

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (SecurityException | MalformedJwtException e) { // 서명 오류, 형식 오류를 더 구체적으로 로깅
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT error: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}