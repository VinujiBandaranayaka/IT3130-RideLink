package com.ridelink.farepayment.service;

import com.ridelink.farepayment.client.RideClient;
import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FareResponse;
import com.ridelink.farepayment.dto.FinalFareRequest;
import com.ridelink.farepayment.dto.RideDto;
import com.ridelink.farepayment.entity.FareEstimate;
import com.ridelink.farepayment.repository.FareEstimateRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FareServiceTest {

    private FareEstimateRepository fareEstimateRepository;
    private IdGeneratorService idGeneratorService;
    private RideClient rideClient;

    private FareService fareService;


    @BeforeEach
    void setUp() {

        fareEstimateRepository =
                mock(FareEstimateRepository.class);

        idGeneratorService =
                mock(IdGeneratorService.class);

        rideClient =
                mock(RideClient.class);

        fareService =
                new FareService(
                        fareEstimateRepository,
                        idGeneratorService,
                        rideClient
                );
    }


    @Test
    void estimateFareShouldUseRideServiceData() {

        // Arrange
        FareEstimateRequest request =
                new FareEstimateRequest(
                        "RIDE001",
                        new BigDecimal("10.00"),
                        new BigDecimal("20.00")
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
                idGeneratorService.generateId("fare_estimate")
        ).thenReturn(1L);

        when(
                fareEstimateRepository.save(any(FareEstimate.class))
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );


        // Act
        FareResponse response =
                fareService.estimateFare(request);


        // Assert response
        assertNotNull(response);

        assertEquals(
                "RIDE001",
                response.rideId()
        );

        assertEquals(
                new BigDecimal("700.00"),
                response.amount()
        );

        assertEquals(
                "LKR",
                response.currency()
        );

        assertNotNull(
                response.breakdown()
        );


        // Verify Ride Service was called
        verify(rideClient)
                .getRideById("RIDE001");


        // Verify fare estimate was saved
        ArgumentCaptor<FareEstimate> fareCaptor =
                ArgumentCaptor.forClass(FareEstimate.class);

        verify(fareEstimateRepository)
                .save(fareCaptor.capture());

        FareEstimate savedFare =
                fareCaptor.getValue();

        assertEquals(
                "RIDE001",
                savedFare.getRideId()
        );

        assertEquals(
                new BigDecimal("10.00"),
                savedFare.getDistanceKm()
        );

        assertEquals(
                new BigDecimal("20.00"),
                savedFare.getDurationMin()
        );

        assertEquals(
                new BigDecimal("700.00"),
                savedFare.getEstimatedAmount()
        );
    }


    @Test
    void finalFareWithPeakSurgeShouldBe1050() {

        // Arrange
        FinalFareRequest request =
                new FinalFareRequest(
                        "RIDE001",
                        new BigDecimal("10.00"),
                        new BigDecimal("20.00"),
                        new BigDecimal("1.5")
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


        // Act
        FareResponse response =
                fareService.calculateFinalFare(request);


        // Assert
        assertNotNull(response);

        assertEquals(
                "RIDE001",
                response.rideId()
        );

        assertEquals(
                new BigDecimal("1050.00"),
                response.amount()
        );

        assertEquals(
                new BigDecimal("1.5"),
                response.breakdown().surgeMultiplier()
        );

        assertEquals(
                new BigDecimal("350.00"),
                response.breakdown().surgeAmount()
        );

        assertEquals(
                new BigDecimal("1050.00"),
                response.breakdown().totalFare()
        );


        verify(rideClient)
                .getRideById("RIDE001");

        // Final fare calculation itself
        // does not save a new estimate document.
        verifyNoInteractions(idGeneratorService);

        verify(
                fareEstimateRepository,
                never()
        ).save(any());
    }
}