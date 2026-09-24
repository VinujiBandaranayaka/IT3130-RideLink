package com.ridelink.farepayment.dto;

import com.ridelink.farepayment.entity.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(
        name = "PaymentRequest",
        description = "Request data required to process a simulated payment"
)
public record PaymentRequest(

        @Schema(
                description = "Unique identifier of the ride",
                example = "RIDE001"
        )
        @NotBlank(message = "Ride ID is required")
        String rideId,


        @Schema(
                description = "Unique identifier of the passenger",
                example = "USER001"
        )
        @NotBlank(message = "Passenger ID is required")
        String passengerId,


        @Schema(
                description = "Unique identifier of the driver",
                example = "DRIVER001"
        )
        @NotBlank(message = "Driver ID is required")
        String driverId,


        @Schema(
                description = "Payment amount in LKR. Amounts above 100000 trigger the simulated payment failure rule",
                example = "700.00"
        )
        @NotNull(message = "Payment amount is required")
        @Positive(message = "Payment amount must be greater than zero")
        BigDecimal amount,


        @Schema(
                description = "Simulated payment method",
                example = "SIMULATED_CARD",
                allowableValues = {
                        "SIMULATED_CARD",
                        "SIMULATED_WALLET"
                }
        )
        @NotNull(message = "Payment method is required")
        PaymentMethod method

) {
}