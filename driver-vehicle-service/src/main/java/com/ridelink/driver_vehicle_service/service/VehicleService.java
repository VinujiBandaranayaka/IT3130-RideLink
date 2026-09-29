package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.exception.ResourceNotFoundException;
import com.ridelink.driver_vehicle_service.model.Vehicle;
import com.ridelink.driver_vehicle_service.repository.DriverRepository;
import com.ridelink.driver_vehicle_service.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public VehicleService(
            VehicleRepository vehicleRepository,
            DriverRepository driverRepository) {

        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    public Vehicle createVehicle(String driverId, Vehicle vehicle) {

        // Check whether the driver exists
        if (!driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException(
                    "Driver not found with id: " + driverId
            );
        }

        // Prevent duplicate vehicle registration numbers
        if (vehicleRepository.existsByRegistrationNumber(
                vehicle.getRegistrationNumber())) {

            throw new IllegalArgumentException(
                    "Vehicle registration number already exists"
            );
        }

        vehicle.setDriverId(driverId);

        return vehicleRepository.save(vehicle);
    }

    public Optional<Vehicle> getVehicleByDriverId(String driverId) {
        return vehicleRepository.findByDriverId(driverId);
    }

    public Optional<Vehicle> updateVehicle(
            String driverId,
            Vehicle updatedVehicle) {

        Optional<Vehicle> existingVehicle =
                vehicleRepository.findByDriverId(driverId);

        if (existingVehicle.isPresent()) {

            Vehicle vehicle = existingVehicle.get();

            vehicle.setRegistrationNumber(
                    updatedVehicle.getRegistrationNumber());

            vehicle.setModel(
                    updatedVehicle.getModel());

            vehicle.setVehicleType(
                    updatedVehicle.getVehicleType());

            vehicle.setCapacity(
                    updatedVehicle.getCapacity());

            return Optional.of(
                    vehicleRepository.save(vehicle)
            );
        }

        return Optional.empty();
    }

    public boolean deleteVehicle(String driverId) {

        Optional<Vehicle> vehicle =
                vehicleRepository.findByDriverId(driverId);

        if (vehicle.isPresent()) {
            vehicleRepository.delete(vehicle.get());
            return true;
        }

        return false;
    }
}