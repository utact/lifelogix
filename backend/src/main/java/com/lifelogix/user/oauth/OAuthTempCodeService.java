package com.lifelogix.user.oauth;

import com.lifelogix.user.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthTempCodeService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(1);
    private static final String CODE_PREFIX = "oauth-code:";

    public String generateAndStore(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long userId = principalDetails.getId();
        String code = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(CODE_PREFIX + code, userId.toString(), CODE_EXPIRATION);
        return code;
    }

    public Long exchange(String code) {
        String key = CODE_PREFIX + code;
        String userIdStr = redisTemplate.opsForValue().get(key);
        if (userIdStr == null) {
            throw new IllegalArgumentException("Invalid or expired authentication code.");
        }
        redisTemplate.delete(key);
        return Long.parseLong(userIdStr);
    }
}
