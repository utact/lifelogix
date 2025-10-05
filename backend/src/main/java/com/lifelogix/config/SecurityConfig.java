package com.lifelogix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // application.yml에 설정한 jwt.secret 값을 주입
    @Value("${jwt.secret}")
    private String secretKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // 2. 세션을 사용하지 않는 Stateless 서버 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. HTTP 요청에 대한 인가 규칙 설정
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 4. OAuth2 리소스 서버 설정을 JWT 방식으로 사용
                .oauth2ResourceServer(oauth2 -> oauth2.jwt());

        return http.build();
    }

    /* *
     * Spring Security가 JWT 토큰을 검증할 때 사용하는 JwtDecoder를 Bean으로 등록
     * -> yml 파일에 설정한 비밀 키를 사용하여 디코더를 생성
     * */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Base64 디코딩된 비밀 키를 사용하여 SecretKeySpec 객체를 생성
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
        // NimbusJwtDecoder를 사용하여 JwtDecoder 인스턴스를 생성하고 반환
        return NimbusJwtDecoder.withSecretKey(secretKeySpec).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
