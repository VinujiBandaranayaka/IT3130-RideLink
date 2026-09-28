package com.ridelink.farepayment.dto;

import java.math.BigDecimal;

public record RideDto(

        String id,

        String passengerId,

        String driverId,

        BigDecimal distanceKm,

        BigDecimal durationMin,

        String status

) {
}