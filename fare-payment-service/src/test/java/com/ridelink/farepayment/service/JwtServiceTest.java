package com.ridelink.farepayment.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "RideLinkTestSecretKey2026ForJwtTesting123";

    private JwtService jwtService;

    private SecretKey secretKey;


    @BeforeEach
    void setUp() {

        jwtService =
                new JwtService(SECRET);

        secretKey =
                Keys.hmacShaKeyFor(
                        SECRET.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }


    @Test
    void validTokenShouldBeAcceptedAndClaimsExtracted() {

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
                        .signWith(secretKey)
                        .compact();


        assertTrue(
                jwtService.isTokenValid(token)
        );

        assertEquals(
                "USER001",
                jwtService.extractUsername(token)
        );

        assertEquals(
                "PASSENGER",
                jwtService.extractRole(token)
        );
    }


    @Test
    void invalidTokenShouldBeRejected() {

        String invalidToken =
                "this.is.not.a.valid.jwt";

        assertFalse(
                jwtService.isTokenValid(invalidToken)
        );
    }


    @Test
    void expiredTokenShouldBeRejected() {

        String expiredToken =
                Jwts.builder()
                        .subject("USER001")
                        .claim("role", "PASSENGER")
                        .issuedAt(
                                new Date(
                                        System.currentTimeMillis()
                                                - 120000
                                )
                        )
                        .expiration(
                                new Date(
                                        System.currentTimeMillis()
                                                - 60000
                                )
                        )
                        .signWith(secretKey)
                        .compact();


        assertFalse(
                jwtService.isTokenValid(expiredToken)
        );
    }
}