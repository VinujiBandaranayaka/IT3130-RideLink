package com.ridelink.farepayment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // REST API uses JWT, so CSRF is not required
                .csrf(csrf ->
                        csrf.disable()
                )

                // JWT = stateless authentication
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exceptions -> exceptions

        // No JWT / invalid JWT
                .authenticationEntryPoint(
                       (request, response, authException) ->
                        response.sendError(
                                HttpServletResponse.SC_UNAUTHORIZED,
                                "Unauthorized"
                        )
                )

        // Valid JWT but wrong role
                .accessDeniedHandler(
                       (request, response, accessDeniedException) ->
                        response.sendError(
                                HttpServletResponse.SC_FORBIDDEN,
                                "Forbidden"
                        )
               )
             )

                // Endpoint security rules
                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // PUBLIC ENDPOINTS
                        // =========================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/fares/estimate"
                        ).permitAll()

                        .requestMatchers(
                                "/actuator/health",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()


                        // =========================
                        // RECEIPT
                        // PASSENGER OR ADMIN
                        // =========================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/payments/*/receipt"
                        ).hasAnyRole(
                                "PASSENGER",
                                "ADMIN"
                        )


                        // =========================
                        // PAYMENT ENDPOINTS
                        // PASSENGER ONLY
                        // =========================

                        .requestMatchers(
                                "/api/payments/**"
                        ).hasRole("PASSENGER")


                        // =========================
                        // EVERYTHING ELSE
                        // =========================

                        .anyRequest()
                        .authenticated()
                )

                // Run our JWT filter before
                // Spring's username/password filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}