package com.ridelink.farepayment.service;

import com.ridelink.farepayment.client.RideClient;
import com.ridelink.farepayment.dto.PaymentRequest;
import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.entity.PaymentMethod;
import com.ridelink.farepayment.entity.PaymentStatus;
import com.ridelink.farepayment.exception.PaymentFailedException;
import com.ridelink.farepayment.repository.PaymentRepository;
import com.ridelink.farepayment.repository.ReceiptRepository;
import com.ridelink.farepayment.dto.RideDto;
import com.ridelink.farepayment.dto.PaymentResponse;
import com.ridelink.farepayment.entity.Receipt;
import com.ridelink.farepayment.dto.ReceiptResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private ReceiptRepository receiptRepository;
    private IdGeneratorService idGeneratorService;
    private RideClient rideClient;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {

        paymentRepository =
                mock(PaymentRepository.class);

        receiptRepository =
                mock(ReceiptRepository.class);

        idGeneratorService =
                mock(IdGeneratorService.class);

        rideClient =
                mock(RideClient.class);

        paymentService =
                new PaymentService(
                        paymentRepository,
                        receiptRepository,
                        idGeneratorService,
                        rideClient
                );
    }


    @Test
    void paymentAboveMaximumAmountShouldFail() {

        // Arrange
        PaymentRequest request =
                new PaymentRequest(
                        "RIDE002",
                        "USER001",
                        "DRIVER001",
                        new BigDecimal("150000.00"),
                        PaymentMethod.SIMULATED_CARD
                );

        when(
                idGeneratorService.generateId("payment")
        ).thenReturn(1L);

        when(
                paymentRepository.save(any(Payment.class))
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );


        // Act + Assert
        assertThrows(
                PaymentFailedException.class,
                () -> paymentService.processPayment(request)
        );


        // Capture payment saved to MongoDB repository
        ArgumentCaptor<Payment> paymentCaptor =
                ArgumentCaptor.forClass(Payment.class);

        verify(paymentRepository)
                .save(paymentCaptor.capture());

        Payment savedPayment =
                paymentCaptor.getValue();


        // Verify FAILED payment details
        assertEquals(
                PaymentStatus.FAILED,
                savedPayment.getStatus()
        );

        assertEquals(
                "RIDE002",
                savedPayment.getRideId()
        );

        assertEquals(
                new BigDecimal("150000.00"),
                savedPayment.getAmount()
        );

        assertNotNull(
                savedPayment.getTransactionRef()
        );


        // Failed payment must NOT create receipt
        verifyNoInteractions(receiptRepository);


        // Amount fails before Ride Service validation
        verifyNoInteractions(rideClient);
    }

    @Test
    void validCompletedRidePaymentShouldSucceed() {

    // Arrange
    PaymentRequest request =
            new PaymentRequest(
                    "RIDE001",
                    "USER001",
                    "DRIVER001",
                    new BigDecimal("700.00"),
                    PaymentMethod.SIMULATED_CARD
            );

    RideDto ride =
            new RideDto(
                    "RIDE001",
                    "USER001",
                    "DRIVER001",
                    new BigDecimal("10.00"),
                    new BigDecimal("20.00"),
                    "COMPLETED"
            );

    when(
            rideClient.getRideById("RIDE001")
    ).thenReturn(ride);

    when(
            idGeneratorService.generateId("payment")
    ).thenReturn(2L);

    when(
            idGeneratorService.generateId("receipt")
    ).thenReturn(10L);

    when(
            paymentRepository.save(any(Payment.class))
    ).thenAnswer(invocation ->
            invocation.getArgument(0)
    );

    when(
            receiptRepository.save(any(Receipt.class))
    ).thenAnswer(invocation ->
            invocation.getArgument(0)
    );


    // Act
    PaymentResponse response =
            paymentService.processPayment(request);


    // Assert response
    assertNotNull(response);

    assertEquals(
            2L,
            response.paymentId()
    );

    assertEquals(
            "RIDE001",
            response.rideId()
    );

    assertEquals(
            new BigDecimal("700.00"),
            response.amount()
    );

    assertEquals(
            PaymentStatus.SUCCESS,
            response.status()
    );

    assertNotNull(
            response.transactionRef()
    );


    // Verify Ride Service was checked
    verify(rideClient)
            .getRideById("RIDE001");


    // Verify successful payment was saved
    ArgumentCaptor<Payment> paymentCaptor =
            ArgumentCaptor.forClass(Payment.class);

    verify(paymentRepository)
            .save(paymentCaptor.capture());

    Payment savedPayment =
            paymentCaptor.getValue();

    assertEquals(
            PaymentStatus.SUCCESS,
            savedPayment.getStatus()
    );

    assertEquals(
            "USER001",
            savedPayment.getPassengerId()
    );

    assertEquals(
            "DRIVER001",
            savedPayment.getDriverId()
    );


    // Verify receipt was created
    ArgumentCaptor<Receipt> receiptCaptor =
            ArgumentCaptor.forClass(Receipt.class);

    verify(receiptRepository)
            .save(receiptCaptor.capture());

    Receipt savedReceipt =
            receiptCaptor.getValue();

    assertEquals(
            2L,
            savedReceipt.getPaymentId()
    );

    assertEquals(
            new BigDecimal("700.00"),
            savedReceipt.getAmount()
    );

    assertNotNull(
            savedReceipt.getReceiptNumber()
    );
    }

    @Test
    void successfulPaymentShouldReturnReceipt() {

    // Arrange
    Payment payment = new Payment();

    payment.setId(2L);
    payment.setRideId("RIDE001");
    payment.setPassengerId("USER001");
    payment.setDriverId("DRIVER001");
    payment.setAmount(new BigDecimal("700.00"));
    payment.setCurrency("LKR");
    payment.setStatus(PaymentStatus.SUCCESS);
    payment.setMethod(PaymentMethod.SIMULATED_CARD);
    payment.setTransactionRef("TXN-001");
    payment.setCreatedAt(LocalDateTime.now());
    payment.setUpdatedAt(LocalDateTime.now());


    Map<String, BigDecimal> breakdown =
            Map.of(
                    "baseFare",
                    new BigDecimal("100.00"),

                    "distanceFare",
                    new BigDecimal("500.00"),

                    "timeFare",
                    new BigDecimal("100.00"),

                    "surgeMultiplier",
                    new BigDecimal("1.0"),

                    "surgeAmount",
                    new BigDecimal("0.00"),

                    "totalFare",
                    new BigDecimal("700.00")
            );


    Receipt receipt = new Receipt();

    receipt.setId(10L);
    receipt.setPaymentId(2L);
    receipt.setReceiptNumber("REC-TEST-001");
    receipt.setIssuedAt(LocalDateTime.now());
    receipt.setAmount(new BigDecimal("700.00"));
    receipt.setBreakdown(breakdown);


    when(
            paymentRepository.findById(2L)
    ).thenReturn(
            Optional.of(payment)
    );

    when(
            receiptRepository.findByPaymentId(2L)
    ).thenReturn(
            Optional.of(receipt)
    );


    // Act
    ReceiptResponse response =
            paymentService.getReceipt(2L);


    // Assert
    assertNotNull(response);

    assertEquals(
            "REC-TEST-001",
            response.receiptNumber()
    );

    assertEquals(
            2L,
            response.paymentId()
    );

    assertEquals(
            new BigDecimal("700.00"),
            response.amount()
    );

    assertNotNull(
            response.issuedAt()
    );

    assertEquals(
            new BigDecimal("700.00"),
            response.breakdown().get("totalFare")
    );


    // Verify repositories were called
    verify(paymentRepository)
            .findById(2L);

    verify(receiptRepository)
            .findByPaymentId(2L);
}
}