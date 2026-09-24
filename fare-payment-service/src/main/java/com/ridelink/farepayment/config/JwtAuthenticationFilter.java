package com.ridelink.farepayment.config;

import com.ridelink.farepayment.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Read Authorization header
        String authorizationHeader =
                request.getHeader("Authorization");

        // If there is no Bearer token,
        // continue to the next filter
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // Remove "Bearer " and keep only the JWT
        String token =
                authorizationHeader.substring(7);

        // Check whether JWT is valid
        if (!jwtService.isTokenValid(token)) {

            filterChain.doFilter(request, response);
            return;
        }

        // Extract user and role
        String username =
                jwtService.extractUsername(token);

        String role =
                jwtService.extractRole(token);

        // If required JWT information is missing,
        // do not authenticate the request
        if (username == null
                || username.isBlank()
                || role == null
                || role.isBlank()) {

            filterChain.doFilter(request, response);
            return;
        }

        // Spring Security normally expects ROLE_PASSENGER,
        // ROLE_ADMIN, etc.
        String springRole =
                role.startsWith("ROLE_")
                        ? role
                        : "ROLE_" + role;

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(springRole);

        // Create authenticated user
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(authority)
                );

        // Store authenticated user in Spring Security
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        // Continue request
        filterChain.doFilter(request, response);
    }
}