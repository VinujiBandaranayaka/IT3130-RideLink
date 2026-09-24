package com.ridelink.farepayment.service;

import com.ridelink.farepayment.client.RideClient;

import com.ridelink.farepayment.dto.FareBreakdown;
import com.ridelink.farepayment.dto.PaymentRequest;
import com.ridelink.farepayment.dto.PaymentResponse;
import com.ridelink.farepayment.dto.ReceiptResponse;
import com.ridelink.farepayment.dto.RideDto;

import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.entity.PaymentStatus;
import com.ridelink.farepayment.entity.Receipt;

import com.ridelink.farepayment.exception.InvalidFareException;
import com.ridelink.farepayment.exception.PaymentFailedException;
import com.ridelink.farepayment.exception.ResourceNotFoundException;

import com.ridelink.farepayment.repository.PaymentRepository;
import com.ridelink.farepayment.repository.ReceiptRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class PaymentService {

    private static final BigDecimal MAX_AMOUNT =
            new BigDecimal("100000.00");

    private static final String CURRENCY = "LKR";

    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final IdGeneratorService idGeneratorService;
    private final RideClient rideClient;
    private final FareCalculator fareCalculator;


    // Constructor injection
    public PaymentService(
            PaymentRepository paymentRepository,
            ReceiptRepository receiptRepository,
            IdGeneratorService idGeneratorService,
            RideClient rideClient) {

        this.paymentRepository = paymentRepository;
        this.receiptRepository = receiptRepository;
        this.idGeneratorService = idGeneratorService;
        this.rideClient = rideClient;

        this.fareCalculator = new FareCalculator();
    }


    // ========================================
    // 1. PROCESS PAYMENT
    // ========================================

    public PaymentResponse processPayment(
            PaymentRequest request) {

        validatePaymentRequest(request);


        // Generate payment ID
        Long paymentId =
                idGeneratorService.generateId("payment");


        // Generate transaction reference
        String transactionRef =
                "TXN-" + UUID.randomUUID();


        LocalDateTime now =
                LocalDateTime.now();


        // ====================================
        // REQUIRED NEGATIVE SCENARIO
        // amount > 100000
        // ====================================

        if (request.amount().compareTo(MAX_AMOUNT) > 0) {

            Payment failedPayment =
                    new Payment(
                            paymentId,
                            request.rideId(),
                            request.passengerId(),
                            request.driverId(),
                            request.amount(),
                            CURRENCY,
                            PaymentStatus.FAILED,
                            request.method(),
                            transactionRef,
                            now,
                            now
                    );


            // Save failed payment
            paymentRepository.save(failedPayment);


            // IMPORTANT:
            // No receipt is generated
            throw new PaymentFailedException(
                    "Payment failed because amount exceeds 100000"
            );
        }


        // ====================================
        // GET TRUSTED RIDE INFORMATION
        // ====================================

        RideDto ride =
                rideClient.getRideById(
                        request.rideId()
                );


        // Verify ride belongs to passenger
        if (!Objects.equals(
                request.passengerId(),
                ride.passengerId())) {

            throw new InvalidFareException(
                    "Passenger does not match the ride"
            );
        }


        // Verify assigned driver
        if (!Objects.equals(
                request.driverId(),
                ride.driverId())) {

            throw new InvalidFareException(
                    "Driver does not match the ride"
            );
        }


        // Payment only after ride completion
        if (ride.status() == null
                || !"COMPLETED".equalsIgnoreCase(
                        ride.status())) {

            throw new InvalidFareException(
                    "Payment can only be processed for a completed ride"
            );
        }


        // Verify payment amount using trusted ride data
        FareBreakdown breakdown =
                verifyFareAmount(
                        ride,
                        request.amount()
                );


        // ====================================
        // SUCCESSFUL PAYMENT
        // ====================================

        Payment payment =
                new Payment(
                        paymentId,
                        ride.id(),
                        ride.passengerId(),
                        ride.driverId(),
                        request.amount(),
                        CURRENCY,
                        PaymentStatus.SUCCESS,
                        request.method(),
                        transactionRef,
                        now,
                        now
                );


        Payment savedPayment =
                paymentRepository.save(payment);


        // Successful payment automatically
        // generates a receipt
        createReceipt(
                savedPayment,
                breakdown
        );


        return toPaymentResponse(
                savedPayment
        );
    }


    // ========================================
    // 2. VERIFY FARE AMOUNT
    // ========================================

    private FareBreakdown verifyFareAmount(
            RideDto ride,
            BigDecimal amount) {


        if (ride.distanceKm() == null
                || ride.durationMin() == null) {

            throw new InvalidFareException(
                    "Ride distance and duration are required"
            );
        }


        // Normal fare
        FareBreakdown normalFare =
                fareCalculator
                        .calculateEstimatedBreakdown(
                                ride.distanceKm(),
                                ride.durationMin()
                        );


        if (amount.compareTo(
                normalFare.totalFare()) == 0) {

            return normalFare;
        }


        // Peak fare
        FareBreakdown peakFare =
                fareCalculator
                        .calculateFinalBreakdown(
                                ride.distanceKm(),
                                ride.durationMin(),
                                new BigDecimal("1.5")
                        );


        if (amount.compareTo(
                peakFare.totalFare()) == 0) {

            return peakFare;
        }


        throw new InvalidFareException(
                "Payment amount does not match the calculated fare"
        );
    }


    // ========================================
    // 3. CREATE RECEIPT
    // ========================================

    private void createReceipt(
            Payment payment,
            FareBreakdown breakdown) {


        Long receiptId =
                idGeneratorService.generateId(
                        "receipt"
                );


        String receiptNumber =
                "REC-" + UUID.randomUUID();


        Map<String, BigDecimal> breakdownMap =
                Map.of(
                        "baseFare",
                        breakdown.baseFare(),

                        "distanceFare",
                        breakdown.distanceFare(),

                        "timeFare",
                        breakdown.timeFare(),

                        "surgeMultiplier",
                        breakdown.surgeMultiplier(),

                        "surgeAmount",
                        breakdown.surgeAmount(),

                        "totalFare",
                        breakdown.totalFare()
                );


        Receipt receipt =
                new Receipt(
                        receiptId,
                        payment.getId(),
                        receiptNumber,
                        LocalDateTime.now(),
                        payment.getAmount(),
                        breakdownMap
                );


        receiptRepository.save(receipt);
    }


    // ========================================
    // 4. GET PAYMENT
    // ========================================

    public PaymentResponse getPayment(
            Long paymentId) {


        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Payment not found with ID: "
                                                        + paymentId
                                        )
                        );


        return toPaymentResponse(payment);
    }


    // ========================================
    // 5. GET RECEIPT
    // ========================================

    public ReceiptResponse getReceipt(
            Long paymentId) {


        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Payment not found with ID: "
                                                        + paymentId
                                        )
                        );


        if (payment.getStatus()
                != PaymentStatus.SUCCESS) {

            throw new ResourceNotFoundException(
                    "Receipt not available for payment: "
                            + paymentId
            );
        }


        Receipt receipt =
                receiptRepository
                        .findByPaymentId(paymentId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Receipt not found for payment: "
                                                        + paymentId
                                        )
                        );


        return new ReceiptResponse(
                receipt.getReceiptNumber(),
                receipt.getPaymentId(),
                receipt.getAmount(),
                receipt.getIssuedAt(),
                receipt.getBreakdown()
        );
    }


    // ========================================
    // 6. VALIDATE REQUEST
    // ========================================

    private void validatePaymentRequest(
            PaymentRequest request) {


        if (request == null) {

            throw new InvalidFareException(
                    "Payment request is required"
            );
        }


        if (request.rideId() == null
                || request.rideId().isBlank()
                || request.passengerId() == null
                || request.passengerId().isBlank()
                || request.driverId() == null
                || request.driverId().isBlank()) {

            throw new InvalidFareException(
                    "Ride, passenger and driver IDs are required"
            );
        }


        if (request.amount() == null
                || request.amount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidFareException(
                    "Payment amount must be greater than zero"
            );
        }


        if (request.method() == null) {

            throw new InvalidFareException(
                    "Payment method is required"
            );
        }
    }


    // ========================================
    // 7. PAYMENT -> RESPONSE
    // ========================================

    private PaymentResponse toPaymentResponse(
            Payment payment) {


        return new PaymentResponse(
                payment.getId(),
                payment.getRideId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getTransactionRef(),
                payment.getCreatedAt()
        );
    }
}