package com.ridelink.farepayment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        name = "ErrorResponse",
        description = "Standard error response returned by the Fare & Payment Service"
)
public record ErrorResponse(

        @Schema(
                description = "Date and time when the error occurred",
                example = "2026-09-24T09:30:00"
        )
        LocalDateTime timestamp,


        @Schema(
                description = "HTTP status code",
                example = "400"
        )
        int status,


        @Schema(
                description = "HTTP error name",
                example = "Bad Request"
        )
        String error,


        @Schema(
                description = "Detailed explanation of the error",
                example = "Payment amount must be greater than zero"
        )
        String message,


        @Schema(
                description = "API endpoint where the error occurred",
                example = "/api/payments"
        )
        String path

) {
}