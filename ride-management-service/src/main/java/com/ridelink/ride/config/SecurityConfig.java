package com.ridelink.ride.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // PASSENGER creates a ride
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/rides"
                        ).hasRole("PASSENGER")

                        // ADMIN assigns driver
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/rides/*/assign"
                        ).hasRole("ADMIN")

                        // DRIVER controls ride lifecycle
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/rides/*/accept",
                                "/api/rides/*/start",
                                "/api/rides/*/complete"
                        ).hasRole("DRIVER")

                        // Passenger, Driver or Admin can cancel
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/rides/*/cancel"
                        ).hasAnyRole(
                                "PASSENGER",
                                "DRIVER",
                                "ADMIN"
                        )

                        // All valid RideLink roles can retrieve rides
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/rides",
                                "/api/rides/**"
                        ).hasAnyRole(
                                "PASSENGER",
                                "DRIVER",
                                "ADMIN"
                        )

                        .anyRequest().permitAll()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return authenticationConverter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {

        SecretKey secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA384"
        );

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS384)
                .build();
    }
}