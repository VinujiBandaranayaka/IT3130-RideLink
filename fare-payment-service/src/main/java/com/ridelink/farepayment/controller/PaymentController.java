package com.ridelink.farepayment.controller;

import com.ridelink.farepayment.dto.PaymentRequest;
import com.ridelink.farepayment.dto.PaymentResponse;
import com.ridelink.farepayment.dto.ReceiptResponse;

import com.ridelink.farepayment.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(
        name = "Payment Management",
        description = "APIs for simulated payments, payment status and receipt retrieval"
)
public class PaymentController {

    private final PaymentService paymentService;


    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    // ========================================
    // 1. PROCESS PAYMENT
    // ========================================

    @Operation(
            summary = "Process simulated payment",
            description = "Processes a simulated payment. "
                    + "Payments above 100000 are recorded as FAILED "
                    + "and return HTTP 402."
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "201",
                    description = "Payment processed successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment request"
            ),

            @ApiResponse(
                    responseCode = "402",
                    description = "Simulated payment failed because amount exceeds 100000"
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Fare estimate not found for the ride"
            )

    })
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response =
                paymentService.processPayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // ========================================
    // 2. GET PAYMENT
    // ========================================

    @Operation(
            summary = "Get payment status",
            description = "Returns payment details and the current payment status using the payment ID."
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "200",
                    description = "Payment retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment ID"
            )

    })
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable("id") Long id) {

        PaymentResponse response =
                paymentService.getPayment(id);

        return ResponseEntity.ok(response);
    }


    // ========================================
    // 3. GET RECEIPT
    // ========================================

    @Operation(
            summary = "Get payment receipt",
            description = "Returns the receipt generated for a successful simulated payment."
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "200",
                    description = "Receipt retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Payment or receipt not found"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment ID"
            )

    })
    @GetMapping("/{id}/receipt")
    public ResponseEntity<ReceiptResponse> getReceipt(
            @PathVariable("id") Long id) {

        ReceiptResponse response =
                paymentService.getReceipt(id);

        return ResponseEntity.ok(response);
    }
}