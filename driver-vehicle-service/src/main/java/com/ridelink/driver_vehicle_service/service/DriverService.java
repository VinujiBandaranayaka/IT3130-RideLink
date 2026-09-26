package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.model.AvailabilityStatus;
import com.ridelink.driver_vehicle_service.model.Driver;
import com.ridelink.driver_vehicle_service.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    // Create driver
    public Driver createDriver(Driver driver) {
        return driverRepository.save(driver);
    }

    // Get all drivers
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    // Get driver by ID
    public Optional<Driver> getDriverById(String id) {
        return driverRepository.findById(id);
    }

    // Update driver
    public Driver updateDriver(String id, Driver updatedDriver) {

        Optional<Driver> existingDriver = driverRepository.findById(id);

        if (existingDriver.isPresent()) {

            Driver driver = existingDriver.get();

            driver.setAccountId(updatedDriver.getAccountId());
            driver.setName(updatedDriver.getName());
            driver.setPhone(updatedDriver.getPhone());
            driver.setAvailability(updatedDriver.getAvailability());
            driver.setServiceArea(updatedDriver.getServiceArea());
            driver.setCurrentLatitude(updatedDriver.getCurrentLatitude());
            driver.setCurrentLongitude(updatedDriver.getCurrentLongitude());

            return driverRepository.save(driver);
        }

        return null;
    }

    // Delete driver
    public boolean deleteDriver(String id) {

        if (driverRepository.existsById(id)) {
            driverRepository.deleteById(id);
            return true;
        }

        return false;
    }

    // Update availability
    public Optional<Driver> updateAvailability(
            String id,
            AvailabilityStatus availability) {

        Optional<Driver> existingDriver = driverRepository.findById(id);

        if (existingDriver.isPresent()) {

            Driver driver = existingDriver.get();
            driver.setAvailability(availability);

            return Optional.of(driverRepository.save(driver));
        }

        return Optional.empty();
    }

    // Update current location
    public Optional<Driver> updateLocation(
            String id,
            Double latitude,
            Double longitude) {

        Optional<Driver> existingDriver = driverRepository.findById(id);

        if (existingDriver.isPresent()) {

            Driver driver = existingDriver.get();

            driver.setCurrentLatitude(latitude);
            driver.setCurrentLongitude(longitude);

            return Optional.of(driverRepository.save(driver));
        }

        return Optional.empty();
    }

    // Get available drivers
    public List<Driver> getAvailableDrivers() {
        return driverRepository.findByAvailability(
                AvailabilityStatus.AVAILABLE
        );
    }

    // Get available drivers by service area
    public List<Driver> getAvailableDriversByArea(String serviceArea) {
        return driverRepository.findByAvailabilityAndServiceArea(
                AvailabilityStatus.AVAILABLE,
                serviceArea
        );
    }
}