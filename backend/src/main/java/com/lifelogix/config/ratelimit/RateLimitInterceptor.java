package com.lifelogix.config.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lifelogix.exception.RateLimitException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // IP 주소별 버킷을 저장하는 캐시 (10분 후 만료)
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = resolveIp(request);
        Bucket bucket = cache.get(ip, this::createNewBucket);

        log.debug("Request from IP: {}. Available Tokens: {}", ip, bucket.getAvailableTokens());

        if (bucket.tryConsume(1)) {
            return true; // 요청 허용
        } else {
            throw new RateLimitException("요청 횟수 제한을 초과했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private Bucket createNewBucket(String key) {
        // 1분에 10개의 토큰을 리필하는 Bandwidth 정의
        Refill refill = Refill.intervally(10, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(10, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveIp(HttpServletRequest request) {
        // X-Forwarded-For 헤더 확인 (리버스 프록시 환경 고려)
        String xffHeader = request.getHeader("X-Forwarded-For");
        if (xffHeader != null && !xffHeader.isEmpty()) {
            return xffHeader.split(",")[0].trim();
        }
        // 헤더가 없을 경우, 직접 연결된 클라이언트 IP 사용
        return request.getRemoteAddr();
    }
}
