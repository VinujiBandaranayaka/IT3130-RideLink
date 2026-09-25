package com.ridelink.driver_vehicle_service.controller;

import com.ridelink.driver_vehicle_service.model.AvailabilityStatus;
import com.ridelink.driver_vehicle_service.model.Driver;
import com.ridelink.driver_vehicle_service.service.DriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    // Create driver
    @PostMapping
    public ResponseEntity<Driver> createDriver(@Valid @RequestBody Driver driver) {
        return new ResponseEntity<>(
                driverService.createDriver(driver),
                HttpStatus.CREATED
        );
    }

    // Get all drivers
    @GetMapping
    public ResponseEntity<List<Driver>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    // Get driver by ID
    @GetMapping("/{id}")
    public ResponseEntity<Driver> getDriverById(@PathVariable String id) {

        return driverService.getDriverById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update driver
    @PutMapping("/{id}")
    public ResponseEntity<Driver> updateDriver(
            @PathVariable String id,
            @Valid @RequestBody Driver driver) {

        Driver updatedDriver = driverService.updateDriver(id, driver);

        if (updatedDriver == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedDriver);
    }

    // Delete driver
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable String id) {

        if (driverService.deleteDriver(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    // Update availability
    @PatchMapping("/{id}/availability")
    public ResponseEntity<Driver> updateAvailability(
            @PathVariable String id,
            @RequestParam AvailabilityStatus availability) {

        return driverService.updateAvailability(id, availability)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update current location
    @PatchMapping("/{id}/location")
    public ResponseEntity<Driver> updateLocation(
            @PathVariable String id,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        return driverService.updateLocation(id, latitude, longitude)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get available drivers
    @GetMapping("/available")
    public ResponseEntity<List<Driver>> getAvailableDrivers() {
        return ResponseEntity.ok(driverService.getAvailableDrivers());
    }

    // Get available drivers by service area
    @GetMapping("/available/{serviceArea}")
    public ResponseEntity<List<Driver>> getAvailableDriversByArea(
            @PathVariable String serviceArea) {

        return ResponseEntity.ok(
                driverService.getAvailableDriversByArea(serviceArea)
        );
    }
}