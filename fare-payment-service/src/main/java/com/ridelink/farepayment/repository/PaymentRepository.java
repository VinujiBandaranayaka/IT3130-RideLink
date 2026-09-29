package com.ridelink.farepayment.repository;

import com.ridelink.farepayment.entity.Payment;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository
        extends MongoRepository<Payment, Long> {

    // Find all payments for a particular ride
    List<Payment> findByRideIdOrderByCreatedAtDesc(String rideId);

    // Find a payment using its transaction reference
    Optional<Payment> findByTransactionRef(String transactionRef);

}