package com.ridelink.farepayment.controller;

import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FinalFareRequest;
import com.ridelink.farepayment.dto.FareResponse;
import com.ridelink.farepayment.service.FareService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fares")
@Tag(
        name = "Fare Management",
        description = "APIs for fare estimation, final fare calculation and fare retrieval"
)
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }


    // ========================================
    // 1. ESTIMATE FARE
    // ========================================

    @Operation(
            summary = "Estimate fare",
            description = "Calculates and stores an estimated fare using ride distance and duration."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fare estimated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid fare input"
            )
    })
    @PostMapping("/estimate")
    public ResponseEntity<FareResponse> estimateFare(
            @Valid @RequestBody FareEstimateRequest request) {

        FareResponse response =
                fareService.estimateFare(request);

        return ResponseEntity.ok(response);
    }


    // ========================================
    // 2. CALCULATE FINAL FARE
    // ========================================

    @Operation(
            summary = "Calculate final fare",
            description = "Calculates the final ride fare using distance, duration and surge multiplier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Final fare calculated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid fare information or surge multiplier"
            )
    })
    @PostMapping("/final")
    public ResponseEntity<FareResponse> calculateFinalFare(
            @Valid @RequestBody FinalFareRequest request) {

        FareResponse response =
                fareService.calculateFinalFare(request);

        return ResponseEntity.ok(response);
    }


    // ========================================
    // 3. GET ESTIMATE BY RIDE ID
    // ========================================

    @Operation(
            summary = "Get fare estimate by ride ID",
            description = "Returns the latest stored fare estimate for the specified ride."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fare estimate retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Fare estimate not found"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ride ID"
            )
    })
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<FareResponse> getEstimateByRideId(
            @PathVariable String rideId) {

        FareResponse response =
                fareService.getEstimateByRideId(rideId);

        return ResponseEntity.ok(response);
    }
}