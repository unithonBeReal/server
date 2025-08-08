package server.config;

import server.security.auth.jwt.JwtAuthenticationFilter;
import server.security.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PERMIT_URL_ARRAY = {
            "/api/v1/auth/login",      // 로그인, 토큰 갱신 등
            "/api/v1/auth/renew",      // 토큰 갱신
            "/swagger-ui/**",       // Swagger UI
            "/v3/api-docs/**",      // Swagger API docs
            "/actuator/prometheus"  // Prometheus 모니터링
    };

    private final JwtService jwtService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_URL_ARRAY).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, Arrays.asList(PERMIT_URL_ARRAY)),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
