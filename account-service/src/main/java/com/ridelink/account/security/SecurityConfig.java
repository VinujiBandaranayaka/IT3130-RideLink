package com.ridelink.account.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    // =========================================
    // PASSWORD ENCODER
    // =========================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================================
    // SECURITY FILTER CHAIN
    // =========================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // REST API + JWT
                .csrf(csrf ->
                        csrf.disable()
                )

                // Stateless authentication
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Endpoint authorization
                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // PUBLIC ACCOUNT ENDPOINTS
                        // =========================

                        .requestMatchers(
                                "/api/accounts",
                                "/api/accounts/login"
                        ).permitAll()


                        // =========================
                        // SWAGGER / OPENAPI
                        // =========================

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()


                        // =========================
                        // ERROR ENDPOINT
                        // =========================

                        .requestMatchers(
                                "/error"
                        ).permitAll()


                        // =========================
                        // ADMIN ONLY
                        // =========================

                        .requestMatchers(
                                "/api/accounts/*/status"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/accounts/*/role"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/accounts/admin/**"
                        ).hasRole("ADMIN")


                        // =========================
                        // EVERYTHING ELSE
                        // =========================

                        .anyRequest()
                        .authenticated()
                )


                // JWT authentication filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}