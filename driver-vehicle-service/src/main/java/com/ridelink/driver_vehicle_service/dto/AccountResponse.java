package com.ridelink.driver_vehicle_service.dto;

public record AccountResponse(
        String id,
        String name,
        String email,
        String role,
        String status
) {
}