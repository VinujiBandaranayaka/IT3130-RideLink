package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.client.AccountClient;
import com.ridelink.driver_vehicle_service.dto.AccountResponse;
import com.ridelink.driver_vehicle_service.model.Driver;
import com.ridelink.driver_vehicle_service.model.Vehicle;
import com.ridelink.driver_vehicle_service.repository.DriverRepository;
import com.ridelink.driver_vehicle_service.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    private static final String DRIVER_ID = "driver123";
    private static final String DRIVER_EMAIL = "driver@test.com";
    private static final String OTHER_EMAIL = "other@test.com";
    private static final String TOKEN = "Bearer test-token";

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private VehicleService vehicleService;

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private Driver createDriver() {

        Driver driver = new Driver();

        driver.setAccountId("account123");
        driver.setName("Test Driver");
        driver.setPhone("0771234567");
        driver.setServiceArea("Colombo");

        return driver;
    }

    private Vehicle createVehicle() {

        Vehicle vehicle = new Vehicle();

        vehicle.setRegistrationNumber("CAB-1234");
        vehicle.setModel("Toyota Prius");
        vehicle.setVehicleType("CAR");
        vehicle.setCapacity(4);

        return vehicle;
    }

    private Authentication driverAuthentication() {

        return new UsernamePasswordAuthenticationToken(
                DRIVER_EMAIL,
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_DRIVER")
                )
        );
    }

    private Authentication otherDriverAuthentication() {

        return new UsernamePasswordAuthenticationToken(
                OTHER_EMAIL,
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_DRIVER")
                )
        );
    }

    private Authentication adminAuthentication() {

        return new UsernamePasswordAuthenticationToken(
                "admin@test.com",
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );
    }

    private AccountResponse driverAccount() {

        return new AccountResponse(
                "account123",
                "Test Driver",
                DRIVER_EMAIL,
                "DRIVER",
                "ACTIVE"
        );
    }

    // =========================================================
    // CREATE VEHICLE
    // =========================================================

    @Test
    void createVehicle_shouldSaveVehicleForOwner() {

        Driver driver = createDriver();
        Vehicle vehicle = createVehicle();

        when(driverRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(driver));

        when(accountClient.getAccountById(
                "account123",
                TOKEN
        )).thenReturn(driverAccount());

        when(vehicleRepository.existsByRegistrationNumber(
                "CAB-1234"
        )).thenReturn(false);

        when(vehicleRepository.save(vehicle))
                .thenReturn(vehicle);

        Vehicle result =
                vehicleService.createVehicle(
                        DRIVER_ID,
                        vehicle,
                        TOKEN,
                        driverAuthentication()
                );

        assertNotNull(result);

        assertEquals(
                DRIVER_ID,
                result.getDriverId()
        );

        assertEquals(
                "CAB-1234",
                result.getRegistrationNumber()
        );

        verify(driverRepository)
                .findById(DRIVER_ID);

        verify(accountClient)
                .getAccountById(
                        "account123",
                        TOKEN
                );

        verify(vehicleRepository)
                .existsByRegistrationNumber("CAB-1234");

        verify(vehicleRepository)
                .save(vehicle);
    }

    // =========================================================
    // CREATE VEHICLE - DRIVER NOT FOUND
    // =========================================================

    @Test
    void createVehicle_shouldRejectNonExistingDriver() {

        Vehicle vehicle = createVehicle();

        when(driverRepository.findById("invalid-driver"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> vehicleService.createVehicle(
                        "invalid-driver",
                        vehicle,
                        TOKEN,
                        driverAuthentication()
                )
        );

        verify(driverRepository)
                .findById("invalid-driver");

        verify(vehicleRepository, never())
                .save(any(Vehicle.class));
    }

    // =========================================================
    // CREATE VEHICLE - DUPLICATE REGISTRATION
    // =========================================================

    @Test
    void createVehicle_shouldRejectDuplicateRegistration() {

        Driver driver = createDriver();
        Vehicle vehicle = createVehicle();

        when(driverRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(driver));

        when(accountClient.getAccountById(
                "account123",
                TOKEN
        )).thenReturn(driverAccount());

        when(vehicleRepository.existsByRegistrationNumber(
                "CAB-1234"
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.createVehicle(
                        DRIVER_ID,
                        vehicle,
                        TOKEN,
                        driverAuthentication()
                )
        );

        verify(vehicleRepository)
                .existsByRegistrationNumber(
                        "CAB-1234"
                );

        verify(vehicleRepository, never())
                .save(any(Vehicle.class));
    }

    // =========================================================
    // CREATE VEHICLE - WRONG OWNER
    // =========================================================

    @Test
    void createVehicle_shouldRejectWrongOwner() {

        Driver driver = createDriver();
        Vehicle vehicle = createVehicle();

        when(driverRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(driver));

        when(accountClient.getAccountById(
                "account123",
                TOKEN
        )).thenReturn(driverAccount());

        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> vehicleService.createVehicle(
                        DRIVER_ID,
                        vehicle,
                        TOKEN,
                        otherDriverAuthentication()
                )
        );

        verify(vehicleRepository, never())
                .save(any(Vehicle.class));
    }

    // =========================================================
    // CREATE VEHICLE - ADMIN
    // =========================================================

    @Test
    void createVehicle_shouldAllowAdmin() {

        Driver driver = createDriver();
        Vehicle vehicle = createVehicle();

        when(driverRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository.existsByRegistrationNumber(
                "CAB-1234"
        )).thenReturn(false);

        when(vehicleRepository.save(vehicle))
                .thenReturn(vehicle);

        Vehicle result =
                vehicleService.createVehicle(
                        DRIVER_ID,
                        vehicle,
                        TOKEN,
                        adminAuthentication()
                );

        assertNotNull(result);

        assertEquals(
                DRIVER_ID,
                result.getDriverId()
        );

        verify(vehicleRepository)
                .save(vehicle);

        // ADMIN does not need AccountClient ownership verification
        verifyNoInteractions(accountClient);
    }

    // =========================================================
    // GET VEHICLE
    // =========================================================

    @Test
    void getVehicleByDriverId_shouldReturnVehicle() {

        Vehicle vehicle = createVehicle();

        vehicle.setDriverId(DRIVER_ID);

        when(vehicleRepository.findByDriverId(DRIVER_ID))
                .thenReturn(Optional.of(vehicle));

        Optional<Vehicle> result =
                vehicleService.getVehicleByDriverId(
                        DRIVER_ID
                );

        assertTrue(result.isPresent());

        assertEquals(
                "CAB-1234",
                result.get().getRegistrationNumber()
        );

        verify(vehicleRepository)
                .findByDriverId(DRIVER_ID);
    }

    // =========================================================
    // UPDATE VEHICLE - OWNER
    // =========================================================

    @Test
    void updateVehicle_shouldUpdateVehicleForOwner() {

        Driver driver = createDriver();

        Vehicle existingVehicle = createVehicle();

        Vehicle updatedVehicle = new Vehicle();

        updatedVehicle.setRegistrationNumber(
                "CAR-9999"
        );

        updatedVehicle.setModel(
                "Honda Vezel"
        );

        updatedVehicle.setVehicleType(
                "SUV"
        );

        updatedVehicle.setCapacity(5);

        when(driverRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(driver));

        when(accountClient.getAccountById(
                "account123",
                TOKEN
        )).thenReturn(driverAccount());

        when(vehicleRepository.findByDriverId(DRIVER_ID))
                .thenReturn(Optional.of(existingVehicle));

        when(vehicleRepository.save(existingVehicle))
                .thenReturn(existingVehicle);

        Optional<Vehicle> result =
                vehicleService.updateVehicle(
                        DRIVER_ID,
                        updatedVehicle,
                        TOKEN,
                        driverAuthentication()
                );

        assertTrue(result.isPresent());

        assertEquals(
                "CAR-9999",
                result.get().getRegistrationNumber()
        );

        assertEquals(
                "Honda Vezel",
                result.get().getModel()
        );

        assertEquals(
                "SUV",
                result.get().getVehicleType()
        );

        assertEquals(
                5,
                result.get().getCapacity()
        );

        verify(vehicleRepository)
                .save(existingVehicle);
    }

    // =========================================================
    // UPDATE VEHICLE - WRONG OWNER
    // =========================================================

    @Test
    void updateVehicle_shouldRejectWrongOwner() {

        Driver driver = createDriver();
        Vehicle vehicle = createVehicle();

        when(driverRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(driver));

        when(accountClient.getAccountById(
                "account123",
                TOKEN
        )).thenReturn(driverAccount());

        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> vehicleService.updateVehicle(
                        DRIVER_ID,
                        vehicle,
                        TOKEN,
                        otherDriverAuthentication()
                )
        );

        verify(vehicleRepository, never())
                .save(any(Vehicle.class));
    }

    // =========================================================
    // UPDATE VEHICLE - NOT FOUND
    // =========================================================

    @Test
    void updateVehicle_shouldReturnEmptyWhenVehicleDoesNotExist() {

        Driver driver = createDriver();
        Vehicle updatedVehicle = createVehicle();

        when(driverRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(driver));

        when(accountClient.getAccountById(
                "account123",
                TOKEN
        )).thenReturn(driverAccount());

        when(vehicleRepository.findByDriverId(DRIVER_ID))
                .thenReturn(Optional.empty());

        Optional<Vehicle> result =
                vehicleService.updateVehicle(
                        DRIVER_ID,
                        updatedVehicle,
                        TOKEN,
                        driverAuthentication()
                );

        assertTrue(result.isEmpty());

        verify(vehicleRepository, never())
                .save(any(Vehicle.class));
    }

    // =========================================================
    // DELETE VEHICLE - OWNER
    // =========================================================

    @Test
    void deleteVehicle_shouldDeleteExistingVehicleForOwner() {

        Driver driver = createDriver();
        Vehicle vehicle = createVehicle();

        when(driverRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(driver));

        when(accountClient.getAccountById(
                "account123",
                TOKEN
        )).thenReturn(driverAccount());

        when(vehicleRepository.findByDriverId(DRIVER_ID))
                .thenReturn(Optional.of(vehicle));

        boolean result =
                vehicleService.deleteVehicle(
                        DRIVER_ID,
                        TOKEN,
                        driverAuthentication()
                );

        assertTrue(result);

        verify(vehicleRepository)
                .delete(vehicle);
    }

    // =========================================================
    // DELETE VEHICLE - WRONG OWNER
    // =========================================================

    @Test
    void deleteVehicle_shouldRejectWrongOwner() {

        Driver driver = createDriver();
        Vehicle vehicle = createVehicle();

        when(driverRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(driver));

        when(accountClient.getAccountById(
                "account123",
                TOKEN
        )).thenReturn(driverAccount());

        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> vehicleService.deleteVehicle(
                        DRIVER_ID,
                        TOKEN,
                        otherDriverAuthentication()
                )
        );

        verify(vehicleRepository, never())
                .delete(any(Vehicle.class));
    }

    // =========================================================
    // DELETE VEHICLE - ADMIN
    // =========================================================

    @Test
    void deleteVehicle_shouldAllowAdmin() {

        Driver driver = createDriver();
        Vehicle vehicle = createVehicle();

        when(driverRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository.findByDriverId(DRIVER_ID))
                .thenReturn(Optional.of(vehicle));

        boolean result =
                vehicleService.deleteVehicle(
                        DRIVER_ID,
                        TOKEN,
                        adminAuthentication()
                );

        assertTrue(result);

        verify(vehicleRepository)
                .delete(vehicle);

        verifyNoInteractions(accountClient);
    }
}