package com.ridelink.farepayment;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtTestTokenGeneratorTest {

    @Test
    void generatePassengerToken() {

        String jwtSecret =
                System.getenv("JWT_SECRET");

        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be set and contain at least 32 characters"
            );
        }

        SecretKey key =
                Keys.hmacShaKeyFor(
                        jwtSecret.getBytes(StandardCharsets.UTF_8)
                );

        String token =
                Jwts.builder()
                        .subject("USER001")
                        .claim("role", "PASSENGER")
                        .issuedAt(new Date())
                        .expiration(
                                new Date(
                                        System.currentTimeMillis()
                                                + 60 * 60 * 1000L
                                )
                        )
                        .signWith(key)
                        .compact();

        var claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("USER001", claims.getSubject());
        assertEquals("PASSENGER", claims.get("role", String.class));
    }
}