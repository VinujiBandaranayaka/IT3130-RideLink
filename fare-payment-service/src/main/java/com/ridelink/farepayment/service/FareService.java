package com.ridelink.farepayment.service;

import com.ridelink.farepayment.client.RideClient;

import com.ridelink.farepayment.dto.FareBreakdown;
import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FareResponse;
import com.ridelink.farepayment.dto.FinalFareRequest;
import com.ridelink.farepayment.dto.RideDto;

import com.ridelink.farepayment.entity.FareEstimate;

import com.ridelink.farepayment.exception.InvalidFareException;
import com.ridelink.farepayment.exception.ResourceNotFoundException;

import com.ridelink.farepayment.repository.FareEstimateRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FareService {

    private static final String CURRENCY = "LKR";

    private final FareEstimateRepository fareEstimateRepository;
    private final IdGeneratorService idGeneratorService;
    private final RideClient rideClient;
    private final FareCalculator fareCalculator;


    public FareService(
            FareEstimateRepository fareEstimateRepository,
            IdGeneratorService idGeneratorService,
            RideClient rideClient) {

        this.fareEstimateRepository = fareEstimateRepository;
        this.idGeneratorService = idGeneratorService;
        this.rideClient = rideClient;

        this.fareCalculator = new FareCalculator();
    }


    // ========================================
    // 1. ESTIMATE FARE
    // ========================================

    public FareResponse estimateFare(
            FareEstimateRequest request) {

        validateRideId(request.rideId());


        // Ask Ride Management Service for ride information
        RideDto ride =
                rideClient.getRideById(request.rideId());


        if (ride.distanceKm() == null
                || ride.durationMin() == null) {

            throw new InvalidFareException(
                    "Ride distance and duration are required"
            );
        }


        FareBreakdown breakdown;

        try {

            breakdown =
                    fareCalculator.calculateEstimatedBreakdown(
                            ride.distanceKm(),
                            ride.durationMin()
                    );

        } catch (IllegalArgumentException ex) {

            throw new InvalidFareException(
                    ex.getMessage(),
                    ex
            );
        }


        Long fareId =
                idGeneratorService.generateId(
                        "fare_estimate"
                );


        FareEstimate estimate =
                new FareEstimate(
                        fareId,
                        ride.id(),
                        ride.distanceKm(),
                        ride.durationMin(),
                        breakdown.totalFare(),
                        CURRENCY,
                        LocalDateTime.now()
                );


        FareEstimate savedEstimate =
                fareEstimateRepository.save(estimate);


        return new FareResponse(
                savedEstimate.getRideId(),
                savedEstimate.getEstimatedAmount(),
                savedEstimate.getCurrency(),
                breakdown
        );
    }


    // ========================================
    // 2. CALCULATE FINAL FARE
    // ========================================

    public FareResponse calculateFinalFare(
            FinalFareRequest request) {

        validateRideId(request.rideId());


        // Ask Ride Management Service for actual ride information
        RideDto ride =
                rideClient.getRideById(request.rideId());


        if (ride.distanceKm() == null
                || ride.durationMin() == null) {

            throw new InvalidFareException(
                    "Ride distance and duration are required"
            );
        }


        try {

            FareBreakdown breakdown =
                    fareCalculator.calculateFinalBreakdown(
                            ride.distanceKm(),
                            ride.durationMin(),
                            request.surgeMultiplier()
                    );


            return new FareResponse(
                    ride.id(),
                    breakdown.totalFare(),
                    CURRENCY,
                    breakdown
            );

        } catch (IllegalArgumentException ex) {

            throw new InvalidFareException(
                    ex.getMessage(),
                    ex
            );
        }
    }


    // ========================================
    // 3. GET ESTIMATE BY RIDE ID
    // ========================================

    public FareResponse getEstimateByRideId(
            String rideId) {

        validateRideId(rideId);


        FareEstimate estimate =
                fareEstimateRepository
                        .findFirstByRideIdOrderByCreatedAtDesc(
                                rideId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Fare estimate not found for ride: "
                                                + rideId
                                )
                        );


        FareBreakdown breakdown =
                fareCalculator.calculateEstimatedBreakdown(
                        estimate.getDistanceKm(),
                        estimate.getDurationMin()
                );


        return new FareResponse(
                estimate.getRideId(),
                estimate.getEstimatedAmount(),
                estimate.getCurrency(),
                breakdown
        );
    }


    // ========================================
    // 4. VALIDATE RIDE ID
    // ========================================

    private void validateRideId(String rideId) {

        if (rideId == null || rideId.isBlank()) {

            throw new InvalidFareException(
                    "Ride ID cannot be empty"
            );
        }
    }
}