package com.ridelink.driver_vehicle_service.repository;

import com.ridelink.driver_vehicle_service.model.AvailabilityStatus;
import com.ridelink.driver_vehicle_service.model.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DriverRepository extends MongoRepository<Driver, String> {

    List<Driver> findByAvailability(AvailabilityStatus availability);

    List<Driver> findByAvailabilityAndServiceArea(
            AvailabilityStatus availability,
            String serviceArea
    );
}
