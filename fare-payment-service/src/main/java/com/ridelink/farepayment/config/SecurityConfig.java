package com.ridelink.farepayment.config;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }


    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // =================================================
                // CSRF
                // =================================================
                // REST API uses JWT, so CSRF is disabled.

                .csrf(csrf ->
                        csrf.disable()
                )


                // =================================================
                // SESSION MANAGEMENT
                // =================================================
                // JWT authentication is stateless.

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                // =================================================
                // EXCEPTION HANDLING
                // ====================================

                .exceptionHandling(exceptions -> exceptions

                        // No JWT / invalid JWT
                        .authenticationEntryPoint(
                                (request,
                                 response,
                                 authException) ->

                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "Unauthorized"
                                        )
                        )

                        // JWT is valid, but user has wrong role
                        .accessDeniedHandler(
                                (request,
                                 response,
                                 accessDeniedException) ->

                                        response.sendError(
                                                HttpServletResponse.SC_FORBIDDEN,
                                                "Forbidden"
                                        )
                        )
                )


                // =================================================
                // ENDPOINT AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(auth -> auth


                        // =========================================
                        // PUBLIC ENDPOINTS
                        // =========================================

                        .requestMatchers(
                                "/actuator/health",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()


                        // =========================================
                        // FARE ESTIMATE
                        // PASSENGER OR ADMIN
                        // =========================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/fares/estimate"
                        ).hasAnyRole(
                                "PASSENGER",
                                "ADMIN"
                        )


                        // =========================================
                        // FINAL FARE
                        // PASSENGER OR ADMIN
                        // =========================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/fares/final"
                        ).hasAnyRole(
                                "PASSENGER",
                                "ADMIN"
                        )


                        // =========================================
                        // GET FARE BY RIDE
                        // PASSENGER / DRIVER / ADMIN
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/fares/ride/**"
                        ).hasAnyRole(
                                "PASSENGER",
                                "DRIVER",
                                "ADMIN"
                        )


                        // =========================================
                        // RECEIPT
                        // PASSENGER OR ADMIN
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/payments/*/receipt"
                        ).hasAnyRole(
                                "PASSENGER",
                                "ADMIN"
                        )


                        // =========================================
                        // CREATE / PROCESS PAYMENT
                        // PASSENGER OR ADMIN
                        // =========================================

                        .requestMatchers(
                             HttpMethod.POST,
                             "/api/payments"
                        ).hasRole("PASSENGER")


                        // =========================================
                        // GET PAYMENT
                        // PASSENGER ONLY
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/payments/**"
                        ).hasRole("PASSENGER")


                        // =========================================
                        // EVERYTHING ELSE
                        // =========================================

                        .anyRequest()
                        .authenticated()
                )


                // =================================================
                // JWT FILTER
                // =================================================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}