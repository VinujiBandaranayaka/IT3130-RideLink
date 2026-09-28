package com.ridelink.farepayment.dto;

import com.ridelink.farepayment.entity.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(
        name = "PaymentResponse",
        description = "Response containing simulated payment details and current payment status"
)
public record PaymentResponse(

        @Schema(
                description = "Unique identifier of the payment",
                example = "1"
        )
        Long paymentId,


        @Schema(
                description = "Unique identifier of the ride",
                example = "RIDE001"
        )
        String rideId,


        @Schema(
                description = "Payment amount in LKR",
                example = "700.00"
        )
        BigDecimal amount,


        @Schema(
                description = "Current payment status",
                example = "SUCCESS",
                allowableValues = {
                        "PENDING",
                        "SUCCESS",
                        "FAILED"
                }
        )
        PaymentStatus status,


        @Schema(
                description = "Unique transaction reference generated for the payment",
                example = "TXN-7f0f26cd-e940-4f16-ab2f-e627d6124578"
        )
        String transactionRef,


        @Schema(
                description = "Date and time when the payment was created",
                example = "2026-09-24T00:54:20"
        )
        LocalDateTime createdAt

) {
}