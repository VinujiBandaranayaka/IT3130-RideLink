package com.ridelink.farepayment.service;

import com.ridelink.farepayment.dto.FareBreakdown;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FareCalculatorTest {

    private final FareCalculator calculator =
            new FareCalculator();


    // TEST 1: Normal estimated fare

    @Test
    void shouldCalculateEstimatedFare_whenValidInput() {

        BigDecimal result =
                calculator.calculateEstimatedFare(
                        new BigDecimal("10"),
                        new BigDecimal("20")
                );

        assertEquals(
                new BigDecimal("700.00"),
                result
        );
    }


    // TEST 2: Zero distance

    @Test
    void shouldCalculateBaseAndTimeFare_whenDistanceIsZero() {

        BigDecimal result =
                calculator.calculateEstimatedFare(
                        BigDecimal.ZERO,
                        new BigDecimal("20")
                );

        assertEquals(
                new BigDecimal("200.00"),
                result
        );
    }


    // TEST 3: Rounding to two decimal places

    @Test
    void shouldRoundFareToTwoDecimalPlaces() {

        BigDecimal result =
                calculator.calculateEstimatedFare(
                        new BigDecimal("1.2345"),
                        BigDecimal.ZERO
                );

        assertEquals(
                new BigDecimal("161.73"),
                result
        );
    }


    // TEST 4: Peak-hour surge multiplier

    @Test
    void shouldApplyPeakSurge_whenMultiplierIsOnePointFive() {

        BigDecimal result =
                calculator.calculateFinalFare(
                        new BigDecimal("10"),
                        new BigDecimal("20"),
                        new BigDecimal("1.5")
                );

        assertEquals(
                new BigDecimal("1050.00"),
                result
        );
    }


    // TEST 5: Verify fare breakdown

    @Test
    void shouldReturnCorrectBreakdown_whenPeakSurgeApplied() {

        FareBreakdown breakdown =
                calculator.calculateFinalBreakdown(
                        new BigDecimal("10"),
                        new BigDecimal("20"),
                        new BigDecimal("1.5")
                );

        assertEquals(
                new BigDecimal("100.00"),
                breakdown.baseFare()
        );

        assertEquals(
                new BigDecimal("500.00"),
                breakdown.distanceFare()
        );

        assertEquals(
                new BigDecimal("100.00"),
                breakdown.timeFare()
        );

        assertEquals(
                new BigDecimal("1.5"),
                breakdown.surgeMultiplier()
        );

        assertEquals(
                new BigDecimal("350.00"),
                breakdown.surgeAmount()
        );

        assertEquals(
                new BigDecimal("1050.00"),
                breakdown.totalFare()
        );
    }


    // TEST 6: Negative distance

    @Test
    void shouldThrowException_whenDistanceIsNegative() {

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateEstimatedFare(
                        new BigDecimal("-10"),
                        new BigDecimal("20")
                )
        );
    }


    // TEST 7: Invalid surge multiplier

    @Test
    void shouldThrowException_whenSurgeMultiplierIsInvalid() {

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateFinalFare(
                        new BigDecimal("10"),
                        new BigDecimal("20"),
                        new BigDecimal("2.0")
                )
        );
    }

}