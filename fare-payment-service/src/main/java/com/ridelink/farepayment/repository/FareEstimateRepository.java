package com.ridelink.farepayment.repository;

import com.ridelink.farepayment.entity.FareEstimate;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FareEstimateRepository
        extends MongoRepository<FareEstimate, Long> {

    Optional<FareEstimate>
        findFirstByRideIdOrderByCreatedAtDesc(String rideId);

}