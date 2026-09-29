package com.ridelink.farepayment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(
        name = "FinalFareRequest",
        description = "Request data required to calculate the final ride fare"
)
public record FinalFareRequest(

        @Schema(
                description = "Unique identifier of the ride",
                example = "RIDE001"
        )
        @NotBlank(message = "Ride ID is required")
        String rideId,


        @Schema(
                description = "Actual ride distance in kilometers",
                example = "10.0"
        )
        @NotNull(message = "Distance is required")
        @PositiveOrZero(message = "Distance cannot be negative")
        BigDecimal distanceKm,


        @Schema(
                description = "Actual ride duration in minutes",
                example = "20.0"
        )
        @NotNull(message = "Duration is required")
        @PositiveOrZero(message = "Duration cannot be negative")
        BigDecimal durationMin,


        @Schema(
                description = "Fare surge multiplier. Supported values are 1.0 for normal pricing and 1.5 for peak pricing",
                example = "1.5"
        )
        @NotNull(message = "Surge multiplier is required")
        @Positive(message = "Surge multiplier must be positive")
        BigDecimal surgeMultiplier

) {
}