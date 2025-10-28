package com.lifelogix.config;

import com.lifelogix.user.oauth.CustomOAuth2UserService;
import com.lifelogix.user.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.lifelogix.user.oauth.OAuth2AuthenticationSuccessHandler;
import com.lifelogix.config.jwt.JwtProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProperties jwtProperties;
    private final CorsProperties corsProperties;
    private final CustomOAuth2UserService customOauth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;


    @PostConstruct
    public void logJwtSecret() {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isBlank()) {
            log.error("!!! JWT SECRET IS NOT SET !!!");
            return;
        }
        log.info("--- JWT Secret Key Diagnostics ---");
        log.info("Loaded JWT Secret Length: {}", secret.length());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            String sha256hex = toHexString(hash);
            log.info("Loaded JWT Secret SHA-256 Hash: {}", sha256hex);
        } catch (NoSuchAlgorithmException e) {
            log.error("Could not calculate SHA-256 hash for JWT Secret", e);
        }
        log.info("----------------------------------");
    }

    private String toHexString(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/v1/auth/**", "/api/auth/**", "/api/v1/health", "/oauth2/**", "/login/oauth2/code/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .baseUri("/oauth2/authorization")
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                        )
                        .userInfoEndpoint(endpoint -> endpoint
                                .userService(customOauth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                ));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new Converter<Jwt, AbstractAuthenticationToken>() {
            @Override
            public AbstractAuthenticationToken convert(Jwt jwt) {
                Long userId = Long.parseLong(jwt.getSubject());
                return new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            }
        };
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] decodedKey = Base64.getDecoder().decode(jwtProperties.getSecret());
        SecretKeySpec secretKeySpec = new SecretKeySpec(decodedKey, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKeySpec).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}