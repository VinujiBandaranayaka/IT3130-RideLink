package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.client.AccountClient;
import com.ridelink.driver_vehicle_service.dto.AccountResponse;
import com.ridelink.driver_vehicle_service.model.AvailabilityStatus;
import com.ridelink.driver_vehicle_service.model.Driver;
import com.ridelink.driver_vehicle_service.repository.DriverRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final AccountClient accountClient;

    public DriverService(
            DriverRepository driverRepository,
            AccountClient accountClient) {

        this.driverRepository = driverRepository;
        this.accountClient = accountClient;
    }

    // =========================================================
    // CREATE DRIVER
    // =========================================================

    public Driver createDriver(
            Driver driver,
            String authorizationHeader,
            String authenticatedEmail) {

        AccountResponse account =
                accountClient.getAccountById(
                        driver.getAccountId(),
                        authorizationHeader
                );

        if (account == null) {
            throw new IllegalArgumentException(
                    "Account not found"
            );
        }

        // Account must have DRIVER role
        if (!"DRIVER".equalsIgnoreCase(account.role())) {
            throw new IllegalArgumentException(
                    "Account must have DRIVER role"
            );
        }

        // Account must be ACTIVE
        if (!"ACTIVE".equalsIgnoreCase(account.status())) {
            throw new IllegalArgumentException(
                    "Driver account must be ACTIVE"
            );
        }

        // DRIVER can only create a driver profile
        // for their own account.
        verifyOwnership(
                account,
                authenticatedEmail
        );

        return driverRepository.save(driver);
    }

    // =========================================================
    // GET ALL DRIVERS
    // =========================================================

    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    // =========================================================
    // GET DRIVER BY ID
    // =========================================================

    public Optional<Driver> getDriverById(String id) {
        return driverRepository.findById(id);
    }

    // =========================================================
    // UPDATE DRIVER
    // =========================================================

    public Driver updateDriver(
            String id,
            Driver updatedDriver,
            String authorizationHeader,
            String authenticatedEmail) {

        Optional<Driver> existingDriver =
                driverRepository.findById(id);

        if (existingDriver.isEmpty()) {
            return null;
        }

        Driver driver = existingDriver.get();

        /*
         * Verify that the authenticated user owns
         * this driver profile.
         */
        verifyDriverOwnership(
                driver,
                authorizationHeader,
                authenticatedEmail
        );

        /*
         * Do NOT update accountId.
         *
         * This prevents a driver from changing the
         * account that owns the driver profile.
         */
        driver.setName(
                updatedDriver.getName()
        );

        driver.setPhone(
                updatedDriver.getPhone()
        );

        driver.setAvailability(
                updatedDriver.getAvailability()
        );

        driver.setServiceArea(
                updatedDriver.getServiceArea()
        );

        driver.setCurrentLatitude(
                updatedDriver.getCurrentLatitude()
        );

        driver.setCurrentLongitude(
                updatedDriver.getCurrentLongitude()
        );

        return driverRepository.save(driver);
    }

    // =========================================================
    // DELETE DRIVER
    // =========================================================

    /*
     * DELETE /api/drivers/{id}
     *
     * SecurityConfig restricts this endpoint to ADMIN.
     *
     * Therefore no additional ownership check is required
     * here. ADMIN can delete any driver.
     */
    public boolean deleteDriver(String id) {

        if (driverRepository.existsById(id)) {

            driverRepository.deleteById(id);

            return true;
        }

        return false;
    }

    // =========================================================
    // UPDATE AVAILABILITY
    // =========================================================

    public Optional<Driver> updateAvailability(
            String id,
            AvailabilityStatus availability,
            String authorizationHeader,
            String authenticatedEmail) {

        Optional<Driver> existingDriver =
                driverRepository.findById(id);

        if (existingDriver.isEmpty()) {
            return Optional.empty();
        }

        Driver driver = existingDriver.get();

        /*
         * Only the owner can update availability.
         */
        verifyDriverOwnership(
                driver,
                authorizationHeader,
                authenticatedEmail
        );

        driver.setAvailability(
                availability
        );

        return Optional.of(
                driverRepository.save(driver)
        );
    }

    // =========================================================
    // UPDATE CURRENT LOCATION
    // =========================================================

    public Optional<Driver> updateLocation(
            String id,
            Double latitude,
            Double longitude,
            String authorizationHeader,
            String authenticatedEmail) {

        Optional<Driver> existingDriver =
                driverRepository.findById(id);

        if (existingDriver.isEmpty()) {
            return Optional.empty();
        }

        Driver driver = existingDriver.get();

        /*
         * Only the owner can update current location.
         */
        verifyDriverOwnership(
                driver,
                authorizationHeader,
                authenticatedEmail
        );

        driver.setCurrentLatitude(
                latitude
        );

        driver.setCurrentLongitude(
                longitude
        );

        return Optional.of(
                driverRepository.save(driver)
        );
    }

    // =========================================================
    // GET AVAILABLE DRIVERS
    // =========================================================

    public List<Driver> getAvailableDrivers() {

        return driverRepository.findByAvailability(
                AvailabilityStatus.AVAILABLE
        );
    }

    // =========================================================
    // GET AVAILABLE DRIVERS BY SERVICE AREA
    // =========================================================

    public List<Driver> getAvailableDriversByArea(
            String serviceArea) {

        return driverRepository
                .findByAvailabilityAndServiceArea(
                        AvailabilityStatus.AVAILABLE,
                        serviceArea
                );
    }

    // =========================================================
    // VERIFY ACCOUNT OWNERSHIP
    // =========================================================

    private void verifyOwnership(
            AccountResponse account,
            String authenticatedEmail) {

        if (authenticatedEmail == null
                || authenticatedEmail.isBlank()) {

            throw new AccessDeniedException(
                    "Authenticated user could not be identified"
            );
        }

        if (account.email() == null
                || !account.email().equalsIgnoreCase(
                        authenticatedEmail
                )) {

            throw new AccessDeniedException(
                    "You are not authorized to manage this driver account"
            );
        }
    }

    // =========================================================
    // VERIFY DRIVER OWNERSHIP
    // =========================================================

    private void verifyDriverOwnership(
            Driver driver,
            String authorizationHeader,
            String authenticatedEmail) {

        if (driver.getAccountId() == null
                || driver.getAccountId().isBlank()) {

            throw new AccessDeniedException(
                    "Driver account information is missing"
            );
        }

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

        verifyOwnership(
                account,
                authenticatedEmail
        );
    }
}