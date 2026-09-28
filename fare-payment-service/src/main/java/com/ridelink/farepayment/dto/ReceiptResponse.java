package com.ridelink.farepayment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(
        name = "ReceiptResponse",
        description = "Response containing receipt information for a successful simulated payment"
)
public record ReceiptResponse(

        @Schema(
                description = "Unique receipt number",
                example = "REC-7f0f26cd-e940-4f16-ab2f-e627d6124578"
        )
        String receiptNumber,


        @Schema(
                description = "Unique identifier of the associated payment",
                example = "1"
        )
        Long paymentId,


        @Schema(
                description = "Amount paid in LKR",
                example = "700.00"
        )
        BigDecimal amount,


        @Schema(
                description = "Date and time when the receipt was issued",
                example = "2026-09-24T00:54:30"
        )
        LocalDateTime issuedAt,


        @Schema(
                description = "Detailed fare breakdown stored with the receipt",
                example = """
                        {
                          "baseFare": 100.00,
                          "distanceFare": 500.00,
                          "timeFare": 100.00,
                          "surgeMultiplier": 1.0,
                          "surgeAmount": 0.00,
                          "totalFare": 700.00
                        }
                        """
        )
        Map<String, BigDecimal> breakdown

) {
}