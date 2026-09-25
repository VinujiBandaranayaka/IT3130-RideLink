package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.model.Vehicle;
import com.ridelink.driver_vehicle_service.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // Create vehicle
    public Vehicle createVehicle(String driverId, Vehicle vehicle) {
        vehicle.setDriverId(driverId);
        return vehicleRepository.save(vehicle);
    }

    // Get vehicle by driver ID
    public Optional<Vehicle> getVehicleByDriverId(String driverId) {
        return vehicleRepository.findByDriverId(driverId);
    }

    // Update vehicle
    public Optional<Vehicle> updateVehicle(
            String driverId,
            Vehicle updatedVehicle) {

        Optional<Vehicle> existingVehicle =
                vehicleRepository.findByDriverId(driverId);

        if (existingVehicle.isPresent()) {

            Vehicle vehicle = existingVehicle.get();

            vehicle.setRegistrationNumber(
                    updatedVehicle.getRegistrationNumber());

            vehicle.setModel(updatedVehicle.getModel());

            vehicle.setVehicleType(
                    updatedVehicle.getVehicleType());

            vehicle.setCapacity(
                    updatedVehicle.getCapacity());

            return Optional.of(vehicleRepository.save(vehicle));
        }

        return Optional.empty();
    }

    // Delete vehicle
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