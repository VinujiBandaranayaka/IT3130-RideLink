package com.ridelink.driver_vehicle_service.config;

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

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                /*
                 * REST API
                 */
                .csrf(csrf ->
                        csrf.disable()
                )

                /*
                 * JWT is stateless.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Authorization rules.
                 */
                .authorizeHttpRequests(auth -> auth

                        // =====================================
                        // SWAGGER / OPENAPI
                        // =====================================

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()

                        // =====================================
                        // ERROR
                        // =====================================

                        .requestMatchers(
                                "/error"
                        ).permitAll()

                        // =====================================
                        // DRIVER CREATE
                        // DRIVER or ADMIN
                        // =====================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/drivers"
                        ).hasAnyRole(
                                "DRIVER",
                                "ADMIN"
                        )

                        // =====================================
                        // DRIVER UPDATE
                        // DRIVER or ADMIN
                        // =====================================

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/drivers/**"
                        ).hasAnyRole(
                                "DRIVER",
                                "ADMIN"
                        )

                        // =====================================
                        // DRIVER AVAILABILITY
                        // DRIVER or ADMIN
                        // =====================================

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/drivers/*/availability"
                        ).hasAnyRole(
                                "DRIVER",
                                "ADMIN"
                        )

                        // =====================================
                        // DRIVER LOCATION
                        // DRIVER or ADMIN
                        // =====================================

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/drivers/*/location"
                        ).hasAnyRole(
                                "DRIVER",
                                "ADMIN"
                        )

                        // =====================================
                        // DRIVER DELETE
                        // ADMIN ONLY
                        // =====================================

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/drivers/**"
                        ).hasRole("ADMIN")

                        // =====================================
                        // VEHICLE CREATE
                        // DRIVER or ADMIN
                        // =====================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/drivers/*/vehicle"
                        ).hasAnyRole(
                                "DRIVER",
                                "ADMIN"
                        )

                        // =====================================
                        // VEHICLE UPDATE
                        // DRIVER or ADMIN
                        // =====================================

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/drivers/*/vehicle"
                        ).hasAnyRole(
                                "DRIVER",
                                "ADMIN"
                        )

                        // =====================================
                        // VEHICLE DELETE
                        // ADMIN or DRIVER
                        // =====================================

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/drivers/*/vehicle"
                        ).hasAnyRole(
                                "DRIVER",
                                "ADMIN"
                        )

                        // =====================================
                        // READ OPERATIONS
                        //
                        // Required by Ride Service integration.
                        // We will tighten these further when
                        // inter-service JWT propagation is added.
                        // =====================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/drivers/**"
                        ).permitAll()

                        // =====================================
                        // EVERYTHING ELSE
                        // =====================================

                        .anyRequest().authenticated()
                )

                /*
                 * JWT filter must run before Spring's
                 * UsernamePasswordAuthenticationFilter.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}