package com.ridelink.ride.service;

import com.ridelink.ride.exception.RideNotFoundException;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RideService {

    private final RideRepository rideRepository;

    public RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    // Create a new ride
    public Ride createRide(Ride ride) {

        ride.setId(null);
        ride.setDriverId(null);
        ride.setStatus(RideStatus.REQUESTED);

        return rideRepository.save(ride);
    }

    // Get a ride by ID
    public Ride getRideById(String id) {

        return rideRepository.findById(id)
                .orElseThrow(() ->
                        new RideNotFoundException(
                                "Ride not found with id: " + id
                        )
                );
    }

    // Get all rides
    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    // Assign a driver to a ride
    public Ride assignDriver(String id, String driverId) {

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

        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ASSIGNED);

        return rideRepository.save(ride);
    }

    // Driver accepts the ride
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

    // Start the ride
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

    // Complete the ride
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

    // Cancel the ride
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