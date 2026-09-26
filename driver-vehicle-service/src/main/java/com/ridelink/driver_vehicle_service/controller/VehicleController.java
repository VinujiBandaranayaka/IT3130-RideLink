package com.ridelink.driver_vehicle_service.controller;

import com.ridelink.driver_vehicle_service.model.Vehicle;
import com.ridelink.driver_vehicle_service.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/drivers/{driverId}/vehicle")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // Create vehicle
    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(
            @PathVariable String driverId,
            @Valid @RequestBody Vehicle vehicle) {

        return new ResponseEntity<>(
                vehicleService.createVehicle(driverId, vehicle),
                HttpStatus.CREATED
        );
    }

    // Get vehicle
    @GetMapping
    public ResponseEntity<Vehicle> getVehicle(
            @PathVariable String driverId) {

        return vehicleService.getVehicleByDriverId(driverId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update vehicle
    @PutMapping
    public ResponseEntity<Vehicle> updateVehicle(
            @PathVariable String driverId,
            @Valid @RequestBody Vehicle vehicle) {

        return vehicleService.updateVehicle(driverId, vehicle)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete vehicle
    @DeleteMapping
    public ResponseEntity<Void> deleteVehicle(
            @PathVariable String driverId) {

        if (vehicleService.deleteVehicle(driverId)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}