package com.lifelogix.config.jwt;

import com.lifelogix.user.PrincipalDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final JwtProperties jwtProperties;

    @Autowired
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(PrincipalDetails principalDetails) {
        return generateToken(principalDetails, jwtProperties.getAccessTokenValiditySeconds());
    }

    public String generateRefreshToken(PrincipalDetails principalDetails) {
        return generateToken(principalDetails, jwtProperties.getRefreshTokenValiditySeconds());
    }

    private String generateToken(PrincipalDetails principalDetails, long validitySeconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validitySeconds * 1000);

        return Jwts.builder()
                .setSubject(String.valueOf(principalDetails.getId()))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
