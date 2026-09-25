package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.model.Vehicle;
import com.ridelink.driver_vehicle_service.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void createVehicle_shouldSaveVehicle() {

        Vehicle vehicle = new Vehicle();

        vehicle.setRegistrationNumber("CAB-1234");
        vehicle.setModel("Toyota Prius");
        vehicle.setVehicleType("CAR");
        vehicle.setCapacity(4);

        when(vehicleRepository.save(vehicle))
                .thenReturn(vehicle);

        Vehicle result =
                vehicleService.createVehicle("driver123", vehicle);

        assertNotNull(result);
        assertEquals("driver123", result.getDriverId());
        assertEquals("CAB-1234",
                result.getRegistrationNumber());

        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void getVehicleByDriverId_shouldReturnVehicle() {

        Vehicle vehicle = new Vehicle();

        vehicle.setDriverId("driver123");
        vehicle.setRegistrationNumber("CAB-1234");

        when(vehicleRepository.findByDriverId("driver123"))
                .thenReturn(Optional.of(vehicle));

        Optional<Vehicle> result =
                vehicleService.getVehicleByDriverId("driver123");

        assertTrue(result.isPresent());
        assertEquals(
                "CAB-1234",
                result.get().getRegistrationNumber()
        );

        verify(vehicleRepository)
                .findByDriverId("driver123");
    }

    @Test
    void deleteVehicle_shouldDeleteExistingVehicle() {

        Vehicle vehicle = new Vehicle();
        vehicle.setDriverId("driver123");

        when(vehicleRepository.findByDriverId("driver123"))
                .thenReturn(Optional.of(vehicle));

        boolean result =
                vehicleService.deleteVehicle("driver123");

        assertTrue(result);

        verify(vehicleRepository).delete(vehicle);
    }
}