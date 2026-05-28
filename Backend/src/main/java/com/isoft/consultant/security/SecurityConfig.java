package com.isoft.consultant.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 流式 AI 响应（Flux/SSE）异步派发时不再重复鉴权，避免 Access Denied
                .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                // 公开接口
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/chat",
                    "/api/interest-test",
                    "/api/schools/**",
                    "/api/majors/**",
                    "/api/stats/**",
                    "/api/rank/**",
                    "/api/interest-test/**",
                    "/chat",
                    "/interest-test",
                    "/static/**"
                ).permitAll()
                // 需要认证的接口
                .requestMatchers("/api/user/**").authenticated()
                .requestMatchers("/api/favorites/**").authenticated()
                .requestMatchers("/api/plans/**").authenticated()
                .requestMatchers("/api/wizard/**").authenticated()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 其他请求放行
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
