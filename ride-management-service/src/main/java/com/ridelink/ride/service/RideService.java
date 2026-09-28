package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.exception.RideNotFoundException;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final DriverServiceClient driverServiceClient;

    public RideService(
            RideRepository rideRepository,
            DriverServiceClient driverServiceClient) {

        this.rideRepository = rideRepository;
        this.driverServiceClient = driverServiceClient;
    }

    public Ride createRide(Ride ride) {

        ride.setId(null);
        ride.setDriverId(null);
        ride.setStatus(RideStatus.REQUESTED);

        return rideRepository.save(ride);
    }

    public Ride getRideById(String id) {

        return rideRepository.findById(id)
                .orElseThrow(
                        () -> new RideNotFoundException(
                                "Ride not found with id: " + id
                        )
                );
    }

    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    public Ride assignDriver(
            String id,
            String driverId) {

        Ride ride = getRideById(id);

        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new IllegalStateException(
                    "Driver can only be assigned to a REQUESTED ride"
            );
        }

        if (driverId == null || driverId.isBlank()) {
            throw new IllegalArgumentException(
                    "Driver ID is required"
            );
        }

        boolean available =
                driverServiceClient.isDriverAvailable(driverId);

        if (!available) {
            throw new IllegalStateException(
                    "Driver is not available"
            );
        }

        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ASSIGNED);

        return rideRepository.save(ride);
    }

    public Ride acceptRide(String id) {

        Ride ride = getRideById(id);

        if (ride.getStatus() != RideStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "Only an ASSIGNED ride can be accepted"
            );
        }

        ride.setStatus(RideStatus.ACCEPTED);

        return rideRepository.save(ride);
    }

    public Ride startRide(String id) {

        Ride ride = getRideById(id);

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new IllegalStateException(
                    "Only an ACCEPTED ride can be started"
            );
        }

        ride.setStatus(RideStatus.IN_PROGRESS);

        return rideRepository.save(ride);
    }

    public Ride completeRide(
            String id,
            Double distanceKm,
            Double durationMin) {

        Ride ride = getRideById(id);

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Only an IN_PROGRESS ride can be completed"
            );
        }

        if (distanceKm == null || distanceKm < 0) {
            throw new IllegalArgumentException(
                    "Distance must be zero or greater"
            );
        }

        if (durationMin == null || durationMin < 0) {
            throw new IllegalArgumentException(
                    "Duration must be zero or greater"
            );
        }

        ride.setDistanceKm(distanceKm);
        ride.setDurationMin(durationMin);
        ride.setStatus(RideStatus.COMPLETED);

        return rideRepository.save(ride);
    }

    public Ride cancelRide(String id) {

        Ride ride = getRideById(id);

        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new IllegalStateException(
                    "A COMPLETED ride cannot be cancelled"
            );
        }

        if (ride.getStatus() == RideStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Ride is already cancelled"
            );
        }

        ride.setStatus(RideStatus.CANCELLED);

        return rideRepository.save(ride);
    }
}