package com.ridelink.farepayment.service;

import com.ridelink.farepayment.dto.FareBreakdown;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FareCalculator {

    // Fare calculation constants

    private static final BigDecimal BASE_FARE =
            new BigDecimal("100.00");

    private static final BigDecimal PER_KM_RATE =
            new BigDecimal("50.00");

    private static final BigDecimal PER_MINUTE_RATE =
            new BigDecimal("5.00");

    private static final BigDecimal NORMAL_SURGE =
            new BigDecimal("1.0");

    private static final BigDecimal PEAK_SURGE =
            new BigDecimal("1.5");


    // 1. Calculate estimated fare

    public BigDecimal calculateEstimatedFare(
            BigDecimal distanceKm,
            BigDecimal durationMin) {

        validateJourney(distanceKm, durationMin);

        BigDecimal distanceFare =
                distanceKm.multiply(PER_KM_RATE);

        BigDecimal timeFare =
                durationMin.multiply(PER_MINUTE_RATE);

        BigDecimal estimatedFare =
                BASE_FARE
                        .add(distanceFare)
                        .add(timeFare);

        return roundMoney(estimatedFare);
    }


    // 2. Calculate final fare

    public BigDecimal calculateFinalFare(
            BigDecimal distanceKm,
            BigDecimal durationMin,
            BigDecimal surgeMultiplier) {

        validateJourney(distanceKm, durationMin);

        validateSurge(surgeMultiplier);

        BigDecimal distanceFare =
                distanceKm.multiply(PER_KM_RATE);

        BigDecimal timeFare =
                durationMin.multiply(PER_MINUTE_RATE);

        BigDecimal estimatedFare =
                BASE_FARE
                        .add(distanceFare)
                        .add(timeFare);

        BigDecimal finalFare =
                estimatedFare.multiply(surgeMultiplier);

        return roundMoney(finalFare);
    }


    // 3. Return estimated fare breakdown

    public FareBreakdown calculateEstimatedBreakdown(
            BigDecimal distanceKm,
            BigDecimal durationMin) {

        return calculateFinalBreakdown(
                distanceKm,
                durationMin,
                NORMAL_SURGE
        );
    }


    // 4. Return final fare breakdown

    public FareBreakdown calculateFinalBreakdown(
            BigDecimal distanceKm,
            BigDecimal durationMin,
            BigDecimal surgeMultiplier) {

        validateJourney(distanceKm, durationMin);

        validateSurge(surgeMultiplier);

        BigDecimal distanceFare =
                distanceKm.multiply(PER_KM_RATE);

        BigDecimal timeFare =
                durationMin.multiply(PER_MINUTE_RATE);

        BigDecimal estimatedFare =
                BASE_FARE
                        .add(distanceFare)
                        .add(timeFare);

        BigDecimal finalFare =
                roundMoney(
                        estimatedFare.multiply(surgeMultiplier)
                );

        BigDecimal surgeAmount =
                finalFare.subtract(
                        roundMoney(estimatedFare)
                );

        return new FareBreakdown(
                BASE_FARE,
                distanceFare,
                timeFare,
                surgeMultiplier,
                surgeAmount,
                finalFare
        );
    }


    // 5. Validate distance and duration

    private void validateJourney(
            BigDecimal distanceKm,
            BigDecimal durationMin) {

        if (distanceKm == null || durationMin == null) {
            throw new IllegalArgumentException(
                    "Distance and duration cannot be null"
            );
        }

        if (distanceKm.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Distance cannot be negative"
            );
        }

        if (durationMin.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Duration cannot be negative"
            );
        }
    }


    // 6. Validate surge multiplier

    private void validateSurge(
            BigDecimal surgeMultiplier) {

        if (surgeMultiplier == null) {
            throw new IllegalArgumentException(
                    "Surge multiplier cannot be null"
            );
        }

        boolean isNormal =
                surgeMultiplier.compareTo(NORMAL_SURGE) == 0;

        boolean isPeak =
                surgeMultiplier.compareTo(PEAK_SURGE) == 0;

        if (!isNormal && !isPeak) {
            throw new IllegalArgumentException(
                    "Surge multiplier must be 1.0 or 1.5"
            );
        }
    }


    // 7. Round monetary amounts to 2 decimal places

    private BigDecimal roundMoney(BigDecimal amount) {

        return amount.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

}