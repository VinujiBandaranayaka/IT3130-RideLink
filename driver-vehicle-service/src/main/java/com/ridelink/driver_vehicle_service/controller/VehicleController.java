package com.ridelink.driver_vehicle_service.controller;

import com.ridelink.driver_vehicle_service.model.Vehicle;
import com.ridelink.driver_vehicle_service.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers/{driverId}/vehicle")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // =========================================================
    // CREATE VEHICLE
    // =========================================================

    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(
            @PathVariable String driverId,
            @Valid @RequestBody Vehicle vehicle,
            @RequestHeader("Authorization") String authorizationHeader,
            Authentication authentication) {

        return new ResponseEntity<>(
                vehicleService.createVehicle(
                        driverId,
                        vehicle,
                        authorizationHeader,
                        authentication
                ),
                HttpStatus.CREATED
        );
    }

    // =========================================================
    // GET VEHICLE
    // =========================================================

    /*
     * GET remains available for Ride Service/read integration
     * according to the current SecurityConfig.
     */
    @GetMapping
    public ResponseEntity<Vehicle> getVehicle(
            @PathVariable String driverId) {

        return vehicleService
                .getVehicleByDriverId(driverId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // UPDATE VEHICLE
    // =========================================================

    @PutMapping
    public ResponseEntity<Vehicle> updateVehicle(
            @PathVariable String driverId,
            @Valid @RequestBody Vehicle vehicle,
            @RequestHeader("Authorization") String authorizationHeader,
            Authentication authentication) {

        return vehicleService
                .updateVehicle(
                        driverId,
                        vehicle,
                        authorizationHeader,
                        authentication
                )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // DELETE VEHICLE
    // =========================================================

    @DeleteMapping
    public ResponseEntity<Void> deleteVehicle(
            @PathVariable String driverId,
            @RequestHeader("Authorization") String authorizationHeader,
            Authentication authentication) {

        if (vehicleService.deleteVehicle(
                driverId,
                authorizationHeader,
                authentication)) {

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}