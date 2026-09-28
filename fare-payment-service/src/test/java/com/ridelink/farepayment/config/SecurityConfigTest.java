package com.ridelink.farepayment.config;

import com.ridelink.farepayment.controller.PaymentController;
import com.ridelink.farepayment.dto.PaymentResponse;
import com.ridelink.farepayment.dto.ReceiptResponse;
import com.ridelink.farepayment.entity.PaymentStatus;
import com.ridelink.farepayment.service.JwtService;
import com.ridelink.farepayment.service.PaymentService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PaymentController.class,
        properties = {
                "jwt.secret=RideLinkTestSecretKeyForSecurityTests2026"
        }
)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class
})
class SecurityConfigTest {

    private static final String SECRET =
            "RideLinkTestSecretKeyForSecurityTests2026";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;


    // =========================================
    // NO TOKEN -> 401
    // =========================================

    @Test
    void protectedEndpointWithoutTokenShouldReturn401()
            throws Exception {

        mockMvc.perform(
                        get("/api/payments/1")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }


    // =========================================
    // PASSENGER -> PAYMENT ALLOWED
    // =========================================

    @Test
    void passengerShouldAccessPaymentEndpoint()
            throws Exception {

        PaymentResponse response =
                new PaymentResponse(
                        1L,
                        "RIDE001",
                        new BigDecimal("700.00"),
                        PaymentStatus.SUCCESS,
                        "TXN-001",
                        LocalDateTime.now()
                );

        when(
                paymentService.getPayment(1L)
        ).thenReturn(response);

        String token =
                createToken(
                        "USER001",
                        "PASSENGER"
                );

        mockMvc.perform(
                        get("/api/payments/1")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }


    // =========================================
    // ADMIN -> PAYMENT ENDPOINT BLOCKED
    // =========================================

    @Test
    void adminShouldNotAccessPassengerPaymentEndpoint()
            throws Exception {

        String token =
                createToken(
                        "ADMIN001",
                        "ADMIN"
                );

        mockMvc.perform(
                        get("/api/payments/1")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }


    // =========================================
    // ADMIN -> RECEIPT ALLOWED
    // =========================================

    @Test
    void adminShouldAccessReceiptEndpoint()
            throws Exception {

        ReceiptResponse response =
                new ReceiptResponse(
                        "REC-001",
                        1L,
                        new BigDecimal("700.00"),
                        LocalDateTime.now(),
                        Map.of(
                                "totalFare",
                                new BigDecimal("700.00")
                        )
                );

        when(
                paymentService.getReceipt(1L)
        ).thenReturn(response);

        String token =
                createToken(
                        "ADMIN001",
                        "ADMIN"
                );

        mockMvc.perform(
                        get("/api/payments/1/receipt")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }


    // =========================================
    // TEST JWT GENERATOR
    // =========================================

    private String createToken(
            String username,
            String role) {

        SecretKey key =
                Keys.hmacShaKeyFor(
                        SECRET.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 60 * 60 * 1000L
                        )
                )
                .signWith(key)
                .compact();
    }
}