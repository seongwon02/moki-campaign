package com.example.moki_campaign.global.config;

import com.example.moki_campaign.domain.store.repository.StoreRepository;
import com.example.moki_campaign.global.auth.JwtAuthenticationFilter;
import com.example.moki_campaign.global.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final StoreRepository storeRepository;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(AbstractHttpConfigurer::disable)
                
                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // 세션 사용하지 않음 (Stateless)
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // 요청 인가 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/h2-console/**"
                        ).permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                
                // H2 Console 사용을 위한 설정 (개발 환경)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                
                // JWT 인증 필터 추가
                .addFilterBefore(
                        jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, storeRepository, objectMapper);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // 개발 환경: 모든 origin 허용
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
