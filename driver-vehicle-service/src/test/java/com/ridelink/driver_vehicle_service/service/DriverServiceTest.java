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

    private static final String TEST_EMAIL =
            "driver@gmail.com";

    private static final String TEST_ACCOUNT_ID =
            "68da1234abcd5678ef901234";

    // =========================================
    // MOCKS
    // =========================================

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private DriverService driverService;

    // =========================================
    // 1. CREATE DRIVER
    // =========================================

    @Test
    void createDriver_shouldSaveDriver() {

        Driver driver = new Driver();

        driver.setAccountId(TEST_ACCOUNT_ID);
        driver.setName("Test Driver");
        driver.setPhone("0712345678");
        driver.setAvailability(
                AvailabilityStatus.AVAILABLE
        );
        driver.setServiceArea("Colombo");

        AccountResponse account =
                new AccountResponse(
                        TEST_ACCOUNT_ID,
                        "Test Driver",
                        TEST_EMAIL,
                        "DRIVER",
                        "ACTIVE"
                );

        when(
                accountClient.getAccountById(
                        TEST_ACCOUNT_ID,
                        TEST_TOKEN
                )
        ).thenReturn(account);

        when(
                driverRepository.save(driver)
        ).thenReturn(driver);

        Driver result =
                driverService.createDriver(
                        driver,
                        TEST_TOKEN,
                        TEST_EMAIL
                );

        assertNotNull(result);

        assertEquals(
                "Test Driver",
                result.getName()
        );

        assertEquals(
                TEST_ACCOUNT_ID,
                result.getAccountId()
        );

        verify(accountClient)
                .getAccountById(
                        TEST_ACCOUNT_ID,
                        TEST_TOKEN
                );

        verify(driverRepository)
                .save(driver);
    }

    // =========================================
    // 2. CREATE DRIVER - WRONG OWNER
    // =========================================

    @Test
    void createDriver_shouldRejectWrongOwner() {

        Driver driver = new Driver();

        driver.setAccountId(TEST_ACCOUNT_ID);

        AccountResponse account =
                new AccountResponse(
                        TEST_ACCOUNT_ID,
                        "Test Driver",
                        TEST_EMAIL,
                        "DRIVER",
                        "ACTIVE"
                );

        when(
                accountClient.getAccountById(
                        TEST_ACCOUNT_ID,
                        TEST_TOKEN
                )
        ).thenReturn(account);

        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> driverService.createDriver(
                        driver,
                        TEST_TOKEN,
                        "another@gmail.com"
                )
        );

        verify(
                driverRepository,
                never()
        ).save(any());
    }

    // =========================================
    // 3. GET DRIVER BY ID
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

        assertTrue(result.isPresent());

        assertEquals(
                "Test Driver",
                result.get().getName()
        );

        verify(driverRepository)
                .findById("driver123");
    }

    // =========================================
    // 4. UPDATE AVAILABILITY
    // =========================================

    @Test
    void updateAvailability_shouldUpdateDriver() {

        Driver driver = new Driver();

        driver.setAccountId(TEST_ACCOUNT_ID);
        driver.setName("Test Driver");
        driver.setAvailability(
                AvailabilityStatus.AVAILABLE
        );

        AccountResponse account =
                new AccountResponse(
                        TEST_ACCOUNT_ID,
                        "Test Driver",
                        TEST_EMAIL,
                        "DRIVER",
                        "ACTIVE"
                );

        when(
                driverRepository.findById(
                        "driver123"
                )
        ).thenReturn(
                Optional.of(driver)
        );

        when(
                accountClient.getAccountById(
                        TEST_ACCOUNT_ID,
                        TEST_TOKEN
                )
        ).thenReturn(account);

        when(
                driverRepository.save(driver)
        ).thenReturn(driver);

        Optional<Driver> result =
                driverService.updateAvailability(
                        "driver123",
                        AvailabilityStatus.ON_TRIP,
                        TEST_TOKEN,
                        TEST_EMAIL
                );

        assertTrue(result.isPresent());

        assertEquals(
                AvailabilityStatus.ON_TRIP,
                result.get().getAvailability()
        );

        verify(accountClient)
                .getAccountById(
                        TEST_ACCOUNT_ID,
                        TEST_TOKEN
                );

        verify(driverRepository)
                .save(driver);
    }

    // =========================================
    // 5. GET AVAILABLE DRIVERS
    // =========================================

    @Test
    void getAvailableDrivers_shouldReturnAvailableDrivers() {

        Driver driver = new Driver();

        driver.setName("Available Driver");

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
                result.get(0).getAvailability()
        );

        verify(driverRepository)
                .findByAvailability(
                        AvailabilityStatus.AVAILABLE
                );
    }

    // =========================================
    // 6. UPDATE DRIVER - NON EXISTING
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

        Driver driver = new Driver();

        Driver result =
                driverService.updateDriver(
                        "invalid-id",
                        driver,
                        TEST_TOKEN,
                        TEST_EMAIL
                );

        assertNull(result);

        verify(driverRepository)
                .findById("invalid-id");

        verify(
                driverRepository,
                never()
        ).save(any());
    }

    // =========================================
    // 7. UPDATE DRIVER - OWN PROFILE
    // =========================================

    @Test
    void updateDriver_shouldAllowOwner() {

        Driver existingDriver = new Driver();

        existingDriver.setAccountId(TEST_ACCOUNT_ID);
        existingDriver.setName("Old Name");
        existingDriver.setPhone("0711111111");

        Driver updatedDriver = new Driver();

        updatedDriver.setAccountId(
                "another-account-id"
        );
        updatedDriver.setName("New Name");
        updatedDriver.setPhone("0722222222");
        updatedDriver.setAvailability(
                AvailabilityStatus.AVAILABLE
        );
        updatedDriver.setServiceArea("Colombo");
        updatedDriver.setCurrentLatitude(6.9271);
        updatedDriver.setCurrentLongitude(79.8612);

        AccountResponse account =
                new AccountResponse(
                        TEST_ACCOUNT_ID,
                        "Test Driver",
                        TEST_EMAIL,
                        "DRIVER",
                        "ACTIVE"
                );

        when(
                driverRepository.findById(
                        "driver123"
                )
        ).thenReturn(
                Optional.of(existingDriver)
        );

        when(
                accountClient.getAccountById(
                        TEST_ACCOUNT_ID,
                        TEST_TOKEN
                )
        ).thenReturn(account);

        when(
                driverRepository.save(existingDriver)
        ).thenReturn(existingDriver);

        Driver result =
                driverService.updateDriver(
                        "driver123",
                        updatedDriver,
                        TEST_TOKEN,
                        TEST_EMAIL
                );

        assertNotNull(result);

        assertEquals(
                "New Name",
                result.getName()
        );

        assertEquals(
                "0722222222",
                result.getPhone()
        );

        // Account ownership must not change
        assertEquals(
                TEST_ACCOUNT_ID,
                result.getAccountId()
        );

        verify(driverRepository)
                .save(existingDriver);
    }

    // =========================================
    // 8. UPDATE DRIVER - WRONG OWNER
    // =========================================

    @Test
    void updateDriver_shouldRejectWrongOwner() {

        Driver existingDriver = new Driver();

        existingDriver.setAccountId(TEST_ACCOUNT_ID);

        when(
                driverRepository.findById(
                        "driver123"
                )
        ).thenReturn(
                Optional.of(existingDriver)
        );

        AccountResponse account =
                new AccountResponse(
                        TEST_ACCOUNT_ID,
                        "Test Driver",
                        TEST_EMAIL,
                        "DRIVER",
                        "ACTIVE"
                );

        when(
                accountClient.getAccountById(
                        TEST_ACCOUNT_ID,
                        TEST_TOKEN
                )
        ).thenReturn(account);

        Driver updatedDriver = new Driver();

        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> driverService.updateDriver(
                        "driver123",
                        updatedDriver,
                        TEST_TOKEN,
                        "another@gmail.com"
                )
        );

        verify(
                driverRepository,
                never()
        ).save(any());
    }

    // =========================================
    // 9. DELETE NON-EXISTING DRIVER
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
                .existsById("invalid-id");

        verify(
                driverRepository,
                never()
        ).deleteById("invalid-id");
    }
}