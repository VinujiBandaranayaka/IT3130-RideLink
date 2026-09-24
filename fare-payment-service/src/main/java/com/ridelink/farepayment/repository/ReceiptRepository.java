package com.ridelink.farepayment.repository;

import com.ridelink.farepayment.entity.Receipt;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository
        extends MongoRepository<Receipt, Long> {

    // Find receipt using payment ID
    Optional<Receipt> findByPaymentId(Long paymentId);

    // Find receipt using receipt number
    Optional<Receipt> findByReceiptNumber(String receiptNumber);

}