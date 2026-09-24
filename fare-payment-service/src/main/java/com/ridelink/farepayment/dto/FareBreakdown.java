package com.ridelink.farepayment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
        name = "FareBreakdown",
        description = "Detailed breakdown showing how a RideLink fare was calculated"
)
public record FareBreakdown(

        @Schema(
                description = "Fixed base fare charged for every ride",
                example = "100.00"
        )
        BigDecimal baseFare,


        @Schema(
                description = "Distance charge calculated as distanceKm × 50.00",
                example = "500.00"
        )
        BigDecimal distanceFare,


        @Schema(
                description = "Time charge calculated as durationMin × 5.00",
                example = "100.00"
        )
        BigDecimal timeFare,


        @Schema(
                description = "Surge multiplier applied to the estimated fare. Normal is 1.0 and peak is 1.5",
                example = "1.0"
        )
        BigDecimal surgeMultiplier,


        @Schema(
                description = "Additional amount added because of surge pricing",
                example = "0.00"
        )
        BigDecimal surgeAmount,


        @Schema(
                description = "Final calculated fare rounded to two decimal places",
                example = "700.00"
        )
        BigDecimal totalFare

) {
}