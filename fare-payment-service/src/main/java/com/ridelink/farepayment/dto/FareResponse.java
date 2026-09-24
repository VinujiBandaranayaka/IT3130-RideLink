package com.ridelink.farepayment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
        name = "FareResponse",
        description = "Response containing the calculated fare and its detailed breakdown"
)
public record FareResponse(

        @Schema(
                description = "Unique identifier of the ride",
                example = "RIDE001"
        )
        String rideId,


        @Schema(
                description = "Calculated fare amount",
                example = "700.00"
        )
        BigDecimal amount,


        @Schema(
                description = "Currency used for the fare",
                example = "LKR"
        )
        String currency,


        @Schema(
                description = "Detailed breakdown of the fare calculation"
        )
        FareBreakdown breakdown

) {
}