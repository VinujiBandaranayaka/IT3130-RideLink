package com.ridelink.ride.controller;

import com.ridelink.ride.model.Ride;
import com.ridelink.ride.service.RideService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Ride createRide(
            @Valid @RequestBody Ride ride) {

        return rideService.createRide(ride);
    }

    @GetMapping("/{id}")
    public Ride getRideById(
            @PathVariable String id) {

        return rideService.getRideById(id);
    }

    @GetMapping
    public List<Ride> getAllRides() {

        return rideService.getAllRides();
    }

    @PutMapping("/{id}/assign")
    public Ride assignDriver(
            @PathVariable String id,
            @RequestParam String driverId) {

        return rideService.assignDriver(id, driverId);
    }

    @PutMapping("/{id}/accept")
    public Ride acceptRide(
            @PathVariable String id) {

        return rideService.acceptRide(id);
    }

    @PutMapping("/{id}/start")
    public Ride startRide(
            @PathVariable String id) {

        return rideService.startRide(id);
    }

    @PutMapping("/{id}/complete")
    public Ride completeRide(
            @PathVariable String id,
            @RequestParam Double distanceKm,
            @RequestParam Double durationMin) {

        return rideService.completeRide(
                id,
                distanceKm,
                durationMin
        );
    }

    @PutMapping("/{id}/cancel")
    public Ride cancelRide(
            @PathVariable String id) {

        return rideService.cancelRide(id);
    }
}