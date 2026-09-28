package com.ridelink.driver_vehicle_service.repository;

import com.ridelink.driver_vehicle_service.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {

    Optional<Vehicle> findByDriverId(String driverId);

    boolean existsByRegistrationNumber(String registrationNumber);
}