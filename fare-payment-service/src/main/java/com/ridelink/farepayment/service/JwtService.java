package com.ridelink.farepayment.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    private final SecretKey secretKey;


    // ========================================
    // CONSTRUCTOR
    // ========================================

    public JwtService(
            @Value("${jwt.secret}") String jwtSecret) {

        if (jwtSecret == null
                || jwtSecret.length() < 32) {

            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 characters"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }


    // ========================================
    // EXTRACT USERNAME / USER ID
    // ========================================

    public String extractUsername(String token) {

        Claims claims = extractClaims(token);

        return claims.getSubject();
    }


    // ========================================
    // EXTRACT ROLE
    // ========================================

    public String extractRole(String token) {

        Claims claims = extractClaims(token);

        return claims.get(
                "role",
                String.class
        );
    }


    // ========================================
    // VALIDATE TOKEN
    // ========================================

    public boolean isTokenValid(String token) {

        try {

            extractClaims(token);

            return true;

        } catch (JwtException
                 | IllegalArgumentException ex) {

            return false;
        }
    }


    // ========================================
    // READ AND VERIFY CLAIMS
    // ========================================

    private Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}