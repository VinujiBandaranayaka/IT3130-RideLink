package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.client.AccountClient;
import com.ridelink.driver_vehicle_service.dto.AccountResponse;
import com.ridelink.driver_vehicle_service.exception.ResourceNotFoundException;
import com.ridelink.driver_vehicle_service.model.Driver;
import com.ridelink.driver_vehicle_service.model.Vehicle;
import com.ridelink.driver_vehicle_service.repository.DriverRepository;
import com.ridelink.driver_vehicle_service.repository.VehicleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final AccountClient accountClient;

    public VehicleService(
            VehicleRepository vehicleRepository,
            DriverRepository driverRepository,
            AccountClient accountClient) {

        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.accountClient = accountClient;
    }

    // =========================================================
    // CREATE VEHICLE
    // =========================================================

    public Vehicle createVehicle(
            String driverId,
            Vehicle vehicle,
            String authorizationHeader,
            Authentication authentication) {

        Driver driver =
                driverRepository.findById(driverId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Driver not found with id: "
                                                + driverId
                                )
                        );

        /*
         * DRIVER can create a vehicle only for their own
         * driver profile.
         *
         * ADMIN can create a vehicle for any driver.
         */
        verifyDriverOwnership(
                driver,
                authorizationHeader,
                authentication
        );

        // Prevent duplicate registration numbers
        if (vehicleRepository.existsByRegistrationNumber(
                vehicle.getRegistrationNumber())) {

            throw new IllegalArgumentException(
                    "Vehicle registration number already exists"
            );
        }

        vehicle.setDriverId(driverId);

        return vehicleRepository.save(vehicle);
    }

    // =========================================================
    // GET VEHICLE
    // =========================================================

    public Optional<Vehicle> getVehicleByDriverId(
            String driverId) {

        return vehicleRepository.findByDriverId(driverId);
    }

    // =========================================================
    // UPDATE VEHICLE
    // =========================================================

    public Optional<Vehicle> updateVehicle(
            String driverId,
            Vehicle updatedVehicle,
            String authorizationHeader,
            Authentication authentication) {

        /*
         * Verify driver ownership before changing vehicle data.
         */
        Driver driver =
                driverRepository.findById(driverId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Driver not found with id: "
                                                + driverId
                                )
                        );

        verifyDriverOwnership(
                driver,
                authorizationHeader,
                authentication
        );

        Optional<Vehicle> existingVehicle =
                vehicleRepository.findByDriverId(driverId);

        if (existingVehicle.isEmpty()) {
            return Optional.empty();
        }

        Vehicle vehicle = existingVehicle.get();

        vehicle.setRegistrationNumber(
                updatedVehicle.getRegistrationNumber()
        );

        vehicle.setModel(
                updatedVehicle.getModel()
        );

        vehicle.setVehicleType(
                updatedVehicle.getVehicleType()
        );

        vehicle.setCapacity(
                updatedVehicle.getCapacity()
        );

        return Optional.of(
                vehicleRepository.save(vehicle)
        );
    }

    // =========================================================
    // DELETE VEHICLE
    // =========================================================

    public boolean deleteVehicle(
            String driverId,
            String authorizationHeader,
            Authentication authentication) {

        Driver driver =
                driverRepository.findById(driverId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Driver not found with id: "
                                                + driverId
                                )
                        );

        /*
         * DRIVER → own vehicle only
         * ADMIN  → any vehicle
         */
        verifyDriverOwnership(
                driver,
                authorizationHeader,
                authentication
        );

        Optional<Vehicle> vehicle =
                vehicleRepository.findByDriverId(driverId);

        if (vehicle.isPresent()) {

            vehicleRepository.delete(
                    vehicle.get()
            );

            return true;
        }

        return false;
    }

    // =========================================================
    // VERIFY DRIVER OWNERSHIP
    // =========================================================

    private void verifyDriverOwnership(
            Driver driver,
            String authorizationHeader,
            Authentication authentication) {

        /*
         * ADMIN can manage any driver's vehicle.
         */
        if (isAdmin(authentication)) {
            return;
        }

        /*
         * Driver must have an account ID.
         */
        if (driver.getAccountId() == null
                || driver.getAccountId().isBlank()) {

            throw new AccessDeniedException(
                    "Driver account information is missing"
            );
        }

        /*
         * Ask Account Service to verify the driver's account.
         */
        AccountResponse account =
                accountClient.getAccountById(
                        driver.getAccountId(),
                        authorizationHeader
                );

        if (account == null) {

            throw new AccessDeniedException(
                    "Driver account could not be verified"
            );
        }

        /*
         * Compare Account Service email with
         * authenticated JWT subject.
         */
        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new AccessDeniedException(
                    "Authenticated user could not be identified"
            );
        }

        if (account.email() == null
                || !account.email().equalsIgnoreCase(
                        authentication.getName()
                )) {

            throw new AccessDeniedException(
                    "You are not authorized to manage this vehicle"
            );
        }
    }

    // =========================================================
    // ADMIN CHECK
    // =========================================================

    private boolean isAdmin(
            Authentication authentication) {

        return authentication != null
                && authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(
                                authority.getAuthority()
                        )
                );
    }
}