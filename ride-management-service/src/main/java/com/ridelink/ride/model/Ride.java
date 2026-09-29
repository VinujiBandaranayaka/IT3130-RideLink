package com.ridelink.ride.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rides")
public class Ride {

    @Id
    private String id;

    @NotBlank(message = "Passenger ID is required")
    private String passengerId;

    private String driverId;

    @NotBlank(message = "Pickup location is required")
    private String pickup;

    @NotBlank(message = "Destination is required")
    private String destination;

    @PositiveOrZero(message = "Distance cannot be negative")
    private Double distanceKm;

    @PositiveOrZero(message = "Duration cannot be negative")
    private Double durationMin;

    private RideStatus status = RideStatus.REQUESTED;

    public Ride() {
    }

    public Ride(
            String id,
            String passengerId,
            String driverId,
            String pickup,
            String destination,
            Double distanceKm,
            Double durationMin,
            RideStatus status) {

        this.id = id;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.pickup = pickup;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.durationMin = durationMin;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Double getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(Double durationMin) {
        this.durationMin = durationMin;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }
}