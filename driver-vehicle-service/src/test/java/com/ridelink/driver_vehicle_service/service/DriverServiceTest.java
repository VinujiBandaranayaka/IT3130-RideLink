package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.client.AccountClient;
import com.ridelink.driver_vehicle_service.dto.AccountResponse;
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

    private static final String TEST_TOKEN =
            "Bearer test-token";


    // =========================================
    // MOCKS
    // =========================================

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private AccountClient accountClient;


    // Mockito injects:
    // DriverRepository + AccountClient
    // into DriverService
    @InjectMocks
    private DriverService driverService;


    // =========================================
    // 1. CREATE DRIVER
    // =========================================

    @Test
    void createDriver_shouldSaveDriver() {

        Driver driver = new Driver();

        driver.setAccountId(
                "68da1234abcd5678ef901234"
        );

        driver.setName("Test Driver");

        driver.setPhone("0712345678");

        driver.setAvailability(
                AvailabilityStatus.AVAILABLE
        );

        driver.setServiceArea("Colombo");


        // -------------------------------------
        // Mock Account Service response
        // -------------------------------------

        AccountResponse account =
                new AccountResponse(
                        "68da1234abcd5678ef901234",
                        "Test Driver",
                        "driver@gmail.com",
                        "DRIVER",
                        "ACTIVE"
                );


        when(
                accountClient.getAccountById(
                        "68da1234abcd5678ef901234",
                        TEST_TOKEN
                )
        ).thenReturn(account);


        // -------------------------------------
        // Mock Driver Repository
        // -------------------------------------

        when(
                driverRepository.save(driver)
        ).thenReturn(driver);


        // -------------------------------------
        // Execute
        // -------------------------------------

        Driver result =
                driverService.createDriver(
                        driver,
                        TEST_TOKEN
                );


        // -------------------------------------
        // Assertions
        // -------------------------------------

        assertNotNull(result);

        assertEquals(
                "Test Driver",
                result.getName()
        );

        assertEquals(
                "68da1234abcd5678ef901234",
                result.getAccountId()
        );


        // -------------------------------------
        // Verify Account Service was checked
        // -------------------------------------

        verify(accountClient)
                .getAccountById(
                        "68da1234abcd5678ef901234",
                        TEST_TOKEN
                );


        // -------------------------------------
        // Verify Driver was saved
        // -------------------------------------

        verify(driverRepository)
                .save(driver);
    }


    // =========================================
    // 2. GET DRIVER BY ID
    // =========================================

    @Test
    void getDriverById_shouldReturnDriver() {

        Driver driver = new Driver();

        driver.setName("Test Driver");


        when(
                driverRepository.findById(
                        "driver123"
                )
        ).thenReturn(
                Optional.of(driver)
        );


        Optional<Driver> result =
                driverService.getDriverById(
                        "driver123"
                );


        assertTrue(
                result.isPresent()
        );

        assertEquals(
                "Test Driver",
                result.get().getName()
        );


        verify(driverRepository)
                .findById(
                        "driver123"
                );
    }


    // =========================================
    // 3. UPDATE AVAILABILITY
    // =========================================

    @Test
    void updateAvailability_shouldUpdateDriver() {

        Driver driver = new Driver();

        driver.setName("Test Driver");

        driver.setAvailability(
                AvailabilityStatus.AVAILABLE
        );


        when(
                driverRepository.findById(
                        "driver123"
                )
        ).thenReturn(
                Optional.of(driver)
        );


        when(
                driverRepository.save(driver)
        ).thenReturn(driver);


        Optional<Driver> result =
                driverService.updateAvailability(
                        "driver123",
                        AvailabilityStatus.ON_TRIP
                );


        assertTrue(
                result.isPresent()
        );


        assertEquals(
                AvailabilityStatus.ON_TRIP,
                result.get().getAvailability()
        );


        verify(driverRepository)
                .save(driver);
    }


    // =========================================
    // 4. GET AVAILABLE DRIVERS
    // =========================================

    @Test
    void getAvailableDrivers_shouldReturnAvailableDrivers() {

        Driver driver = new Driver();

        driver.setName(
                "Available Driver"
        );

        driver.setAvailability(
                AvailabilityStatus.AVAILABLE
        );


        when(
                driverRepository.findByAvailability(
                        AvailabilityStatus.AVAILABLE
                )
        ).thenReturn(
                List.of(driver)
        );


        List<Driver> result =
                driverService.getAvailableDrivers();


        assertEquals(
                1,
                result.size()
        );


        assertEquals(
                AvailabilityStatus.AVAILABLE,
                result.get(0)
                        .getAvailability()
        );


        verify(driverRepository)
                .findByAvailability(
                        AvailabilityStatus.AVAILABLE
                );
    }


    // =========================================
    // 5. UPDATE NON-EXISTING DRIVER
    // =========================================

    @Test
    void updateDriver_shouldReturnNullWhenDriverDoesNotExist() {

        when(
                driverRepository.findById(
                        "invalid-id"
                )
        ).thenReturn(
                Optional.empty()
        );


        Driver driver =
                new Driver();


        Driver result =
                driverService.updateDriver(
                        "invalid-id",
                        driver
                );


        assertNull(result);


        verify(driverRepository)
                .findById(
                        "invalid-id"
                );


        verify(
                driverRepository,
                never()
        ).save(any());
    }


    // =========================================
    // 6. DELETE NON-EXISTING DRIVER
    // =========================================

    @Test
    void deleteDriver_shouldReturnFalseWhenDriverDoesNotExist() {

        when(
                driverRepository.existsById(
                        "invalid-id"
                )
        ).thenReturn(false);


        boolean result =
                driverService.deleteDriver(
                        "invalid-id"
                );


        assertFalse(result);


        verify(driverRepository)
                .existsById(
                        "invalid-id"
                );


        verify(
                driverRepository,
                never()
        ).deleteById(
                "invalid-id"
        );
    }
}