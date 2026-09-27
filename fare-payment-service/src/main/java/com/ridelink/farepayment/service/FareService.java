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


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public FareService(
            FareEstimateRepository fareEstimateRepository,
            IdGeneratorService idGeneratorService,
            RideClient rideClient) {

        this.fareEstimateRepository = fareEstimateRepository;
        this.idGeneratorService = idGeneratorService;
        this.rideClient = rideClient;

        /*
         * FareCalculator is currently created here because
         * it does not need Spring dependencies.
         */
        this.fareCalculator = new FareCalculator();
    }


    // =========================================================
    // 1. ESTIMATE FARE
    // =========================================================

    public FareResponse estimateFare(
            FareEstimateRequest request,
            String authorizationHeader) {

        // Validate request
        if (request == null) {
            throw new InvalidFareException(
                    "Fare estimate request cannot be null"
            );
        }

        validateRideId(request.rideId());
        validateAuthorizationHeader(authorizationHeader);


        /*
         * Member 4 does NOT access Member 3's MongoDB.
         *
         * Instead:
         *
         * Member 4
         *      |
         *      | GET /api/rides/{rideId}
         *      |
         *      v
         * Member 3 Ride Management Service
         */
        RideDto ride = rideClient.getRideById(
                request.rideId(),
                authorizationHeader
        );


        if (ride == null) {
            throw new InvalidFareException(
                    "Ride information could not be retrieved"
            );
        }


        /*
         * Fare calculation requires distance and duration.
         */
        validateRideDistanceAndDuration(ride);


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


        /*
         * Generate a unique ID for the fare estimate.
         */
        Long fareId =
                idGeneratorService.generateId(
                        "fare_estimate"
                );


        /*
         * Store the fare estimate in Member 4's database.
         *
         * We only store the information that belongs
         * to the Fare & Payment Service.
         */
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


        /*
         * Send the calculated estimate back to the client.
         */
        return new FareResponse(
                savedEstimate.getRideId(),
                savedEstimate.getEstimatedAmount(),
                savedEstimate.getCurrency(),
                breakdown
        );
    }


    // =========================================================
    // 2. CALCULATE FINAL FARE
    // =========================================================

    public FareResponse calculateFinalFare(
            FinalFareRequest request,
            String authorizationHeader) {

        // Validate request
        if (request == null) {
            throw new InvalidFareException(
                    "Final fare request cannot be null"
            );
        }

        validateRideId(request.rideId());
        validateAuthorizationHeader(authorizationHeader);


        /*
         * Get actual ride information from Member 3.
         */
        RideDto ride = rideClient.getRideById(
                request.rideId(),
                authorizationHeader
        );


        if (ride == null) {
            throw new InvalidFareException(
                    "Ride information could not be retrieved"
            );
        }


        /*
         * Final fare must only be calculated
         * after the ride has been completed.
         */
        if (!"COMPLETED".equalsIgnoreCase(ride.status())) {

            throw new InvalidFareException(
                    "Final fare can only be calculated "
                            + "for a completed ride"
            );
        }


        /*
         * Distance and duration should have been supplied
         * when Member 3 completed the ride.
         */
        validateRideDistanceAndDuration(ride);


        /*
         * Surge multiplier is required for final fare.
         */
        if (request.surgeMultiplier() == null) {

            throw new InvalidFareException(
                    "Surge multiplier is required"
            );
        }


        FareBreakdown breakdown;

        try {

            breakdown =
                    fareCalculator.calculateFinalBreakdown(
                            ride.distanceKm(),
                            ride.durationMin(),
                            request.surgeMultiplier()
                    );

        } catch (IllegalArgumentException ex) {

            throw new InvalidFareException(
                    ex.getMessage(),
                    ex
            );
        }


        /*
         * Return final calculated fare.
         *
         * PaymentService can later use this amount
         * when creating the simulated payment.
         */
        return new FareResponse(
                ride.id(),
                breakdown.totalFare(),
                CURRENCY,
                breakdown
        );
    }


    // =========================================================
    // 3. GET LATEST ESTIMATE BY RIDE ID
    // =========================================================

    public FareResponse getEstimateByRideId(
            String rideId) {

        validateRideId(rideId);


        /*
         * Search only Member 4's Fare database.
         */
        FareEstimate estimate =
                fareEstimateRepository
                        .findFirstByRideIdOrderByCreatedAtDesc(
                                rideId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Fare estimate not found "
                                                        + "for ride: "
                                                        + rideId
                                        )
                        );


        FareBreakdown breakdown;

        try {

            breakdown =
                    fareCalculator.calculateEstimatedBreakdown(
                            estimate.getDistanceKm(),
                            estimate.getDurationMin()
                    );

        } catch (IllegalArgumentException ex) {

            throw new InvalidFareException(
                    ex.getMessage(),
                    ex
            );
        }


        return new FareResponse(
                estimate.getRideId(),
                estimate.getEstimatedAmount(),
                estimate.getCurrency(),
                breakdown
        );
    }


    // =========================================================
    // 4. VALIDATE RIDE ID
    // =========================================================

    private void validateRideId(
            String rideId) {

        if (rideId == null
                || rideId.isBlank()) {

            throw new InvalidFareException(
                    "Ride ID cannot be empty"
            );
        }
    }


    // =========================================================
    // 5. VALIDATE AUTHORIZATION HEADER
    // =========================================================

    private void validateAuthorizationHeader(
            String authorizationHeader) {

        if (authorizationHeader == null
                || authorizationHeader.isBlank()) {

            throw new InvalidFareException(
                    "Authorization header is required"
            );
        }


        if (!authorizationHeader.startsWith("Bearer ")) {

            throw new InvalidFareException(
                    "Authorization header must contain a Bearer token"
            );
        }
    }


    // =========================================================
    // 6. VALIDATE RIDE DISTANCE AND DURATION
    // =========================================================

    private void validateRideDistanceAndDuration(
            RideDto ride) {

        if (ride.distanceKm() == null) {

            throw new InvalidFareException(
                    "Ride distance is required "
                            + "for fare calculation"
            );
        }


        if (ride.durationMin() == null) {

            throw new InvalidFareException(
                    "Ride duration is required "
                            + "for fare calculation"
            );
        }


        if (ride.distanceKm().signum() < 0) {

            throw new InvalidFareException(
                    "Ride distance cannot be negative"
            );
        }


        if (ride.durationMin().signum() < 0) {

            throw new InvalidFareException(
                    "Ride duration cannot be negative"
            );
        }
    }
}