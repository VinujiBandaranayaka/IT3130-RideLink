package com.ridelink.farepayment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(
        name = "FareEstimateRequest",
        description = "Request data required to calculate an estimated ride fare"
)
public record FareEstimateRequest(

        @Schema(
                description = "Unique identifier of the ride",
                example = "RIDE001"
        )
        @NotBlank(message = "Ride ID is required")
        String rideId,


        @Schema(
                description = "Estimated ride distance in kilometers",
                example = "10.0"
        )
        @NotNull(message = "Distance is required")
        @PositiveOrZero(message = "Distance cannot be negative")
        BigDecimal distanceKm,


        @Schema(
                description = "Estimated ride duration in minutes",
                example = "20.0"
        )
        @NotNull(message = "Duration is required")
        @PositiveOrZero(message = "Duration cannot be negative")
        BigDecimal durationMin

) {
}