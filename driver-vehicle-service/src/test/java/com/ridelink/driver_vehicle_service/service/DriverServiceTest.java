package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.model.AvailabilityStatus;
import com.ridelink.driver_vehicle_service.model.Driver;
import com.ridelink.driver_vehicle_service.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    @Test
    void createDriver_shouldSaveDriver() {

        Driver driver = new Driver();
        driver.setAccountId(5001L);
        driver.setName("Test Driver");
        driver.setPhone("0712345678");
        driver.setAvailability(AvailabilityStatus.AVAILABLE);
        driver.setServiceArea("Colombo");

        when(driverRepository.save(driver)).thenReturn(driver);

        Driver result = driverService.createDriver(driver);

        assertNotNull(result);
        assertEquals("Test Driver", result.getName());

        verify(driverRepository).save(driver);
    }

    @Test
    void getDriverById_shouldReturnDriver() {

        Driver driver = new Driver();
        driver.setName("Test Driver");

        when(driverRepository.findById("driver123"))
                .thenReturn(Optional.of(driver));

        Optional<Driver> result =
                driverService.getDriverById("driver123");

        assertTrue(result.isPresent());
        assertEquals("Test Driver", result.get().getName());

        verify(driverRepository).findById("driver123");
    }

    @Test
    void updateAvailability_shouldUpdateDriver() {

        Driver driver = new Driver();
        driver.setName("Test Driver");
        driver.setAvailability(AvailabilityStatus.AVAILABLE);

        when(driverRepository.findById("driver123"))
                .thenReturn(Optional.of(driver));

        when(driverRepository.save(driver))
                .thenReturn(driver);

        Optional<Driver> result =
                driverService.updateAvailability(
                        "driver123",
                        AvailabilityStatus.ON_TRIP
                );

        assertTrue(result.isPresent());
        assertEquals(
                AvailabilityStatus.ON_TRIP,
                result.get().getAvailability()
        );

        verify(driverRepository).save(driver);
    }

    @Test
    void getAvailableDrivers_shouldReturnAvailableDrivers() {

        Driver driver = new Driver();
        driver.setName("Available Driver");
        driver.setAvailability(AvailabilityStatus.AVAILABLE);

        when(driverRepository.findByAvailability(
                AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of(driver));

        List<Driver> result =
                driverService.getAvailableDrivers();

        assertEquals(1, result.size());
        assertEquals(
                AvailabilityStatus.AVAILABLE,
                result.get(0).getAvailability()
        );

        verify(driverRepository)
                .findByAvailability(AvailabilityStatus.AVAILABLE);
    }
    @Test
    void updateDriver_shouldReturnNullWhenDriverDoesNotExist() {

        when(driverRepository.findById("invalid-id"))
                .thenReturn(Optional.empty());

        Driver driver = new Driver();

        Driver result =
                driverService.updateDriver(
                        "invalid-id",
                        driver
                );

        assertNull(result);

        verify(driverRepository)
                .findById("invalid-id");
        }

        @Test
        void deleteDriver_shouldReturnFalseWhenDriverDoesNotExist() {

        when(driverRepository.existsById("invalid-id"))
                .thenReturn(false);

        boolean result =
                driverService.deleteDriver("invalid-id");

        assertFalse(result);

        verify(driverRepository)
                .existsById("invalid-id");

        verify(driverRepository, never())
                .deleteById("invalid-id");
        }
}