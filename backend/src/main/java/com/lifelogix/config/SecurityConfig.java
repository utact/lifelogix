package com.lifelogix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API 서버의 Stateless -> CSRF(Cross-Site Request Forgery) 보호 기능 비활성화
                .csrf(csrf -> csrf.disable())

                // HTTP 요청에 대한 인가 규칙 설정
                .authorizeHttpRequests(authz -> authz
                        // "/" 경로를 포함한 모든 요청("/**")에 대해 접근을 허용
                        .requestMatchers("/**").permitAll()
                        // 그 외 나머지 요청은 인증이 필요 (현재는 위 규칙 때문에 미작동)
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}