package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.exception.RideNotFoundException;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverServiceClient driverServiceClient;

    @InjectMocks
    private RideService rideService;

    @Test
    void createRide_shouldCreateRideWithRequestedStatus() {

        Ride ride = new Ride();
        ride.setPassengerId("USER001");
        ride.setPickup("Malabe");
        ride.setDestination("Colombo");

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ride result = rideService.createRide(ride);

        assertNull(result.getId());
        assertNull(result.getDriverId());
        assertEquals(RideStatus.REQUESTED, result.getStatus());

        verify(rideRepository, times(1)).save(ride);
    }

    @Test
    void assignDriver_shouldAssignAvailableDriverToRequestedRide() {

        Ride ride = createRide(
                "ride001",
                RideStatus.REQUESTED
        );

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        when(driverServiceClient.isDriverAvailable("DRIVER001"))
                .thenReturn(true);

        when(rideRepository.save(ride))
                .thenReturn(ride);

        Ride result = rideService.assignDriver(
                "ride001",
                "DRIVER001"
        );

        assertEquals("DRIVER001", result.getDriverId());
        assertEquals(RideStatus.ASSIGNED, result.getStatus());

        verify(driverServiceClient)
                .isDriverAvailable("DRIVER001");

        verify(rideRepository)
                .save(ride);
    }

    @Test
    void assignDriver_shouldRejectUnavailableDriver() {

        Ride ride = createRide(
                "ride001",
                RideStatus.REQUESTED
        );

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        when(driverServiceClient.isDriverAvailable("DRIVER001"))
                .thenReturn(false);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> rideService.assignDriver(
                                "ride001",
                                "DRIVER001"
                        )
                );

        assertEquals(
                "Driver is not available",
                exception.getMessage()
        );

        verify(driverServiceClient)
                .isDriverAvailable("DRIVER001");

        verify(rideRepository, never())
                .save(any());
    }

    @Test
    void assignDriver_shouldRejectRideThatIsNotRequested() {

        Ride ride = createRide(
                "ride001",
                RideStatus.COMPLETED
        );

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> rideService.assignDriver(
                                "ride001",
                                "DRIVER001"
                        )
                );

        assertEquals(
                "Driver can only be assigned to a REQUESTED ride",
                exception.getMessage()
        );

        verify(driverServiceClient, never())
                .isDriverAvailable(anyString());

        verify(rideRepository, never())
                .save(any());
    }

    @Test
    void acceptRide_shouldAcceptAssignedRide() {

        Ride ride = createRide(
                "ride001",
                RideStatus.ASSIGNED
        );

        ride.setDriverId("DRIVER001");

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(ride))
                .thenReturn(ride);

        Ride result =
                rideService.acceptRide("ride001");

        assertEquals(
                RideStatus.ACCEPTED,
                result.getStatus()
        );

        verify(rideRepository).save(ride);
    }

    @Test
    void acceptRide_shouldRejectRequestedRide() {

        Ride ride = createRide(
                "ride001",
                RideStatus.REQUESTED
        );

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        assertThrows(
                IllegalStateException.class,
                () -> rideService.acceptRide("ride001")
        );

        verify(rideRepository, never())
                .save(any());
    }

    @Test
    void startRide_shouldStartAcceptedRide() {

        Ride ride = createRide(
                "ride001",
                RideStatus.ACCEPTED
        );

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(ride))
                .thenReturn(ride);

        Ride result =
                rideService.startRide("ride001");

        assertEquals(
                RideStatus.IN_PROGRESS,
                result.getStatus()
        );

        verify(rideRepository).save(ride);
    }

    @Test
    void completeRide_shouldCompleteInProgressRide() {

        Ride ride = createRide(
                "ride001",
                RideStatus.IN_PROGRESS
        );

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(ride))
                .thenReturn(ride);

        Ride result =
                rideService.completeRide(
                        "ride001",
                        12.5,
                        25.0
                );

        assertEquals(
                RideStatus.COMPLETED,
                result.getStatus()
        );

        assertEquals(
                12.5,
                result.getDistanceKm()
        );

        assertEquals(
                25.0,
                result.getDurationMin()
        );

        verify(rideRepository).save(ride);
    }

    @Test
    void completeRide_shouldRejectRideThatIsNotInProgress() {

        Ride ride = createRide(
                "ride001",
                RideStatus.ACCEPTED
        );

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        assertThrows(
                IllegalStateException.class,
                () -> rideService.completeRide(
                        "ride001",
                        12.5,
                        25.0
                )
        );

        verify(rideRepository, never())
                .save(any());
    }

    @Test
    void cancelRide_shouldCancelRequestedRide() {

        Ride ride = createRide(
                "ride001",
                RideStatus.REQUESTED
        );

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(ride))
                .thenReturn(ride);

        Ride result =
                rideService.cancelRide("ride001");

        assertEquals(
                RideStatus.CANCELLED,
                result.getStatus()
        );

        verify(rideRepository).save(ride);
    }

    @Test
    void cancelRide_shouldRejectCompletedRide() {

        Ride ride = createRide(
                "ride001",
                RideStatus.COMPLETED
        );

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> rideService.cancelRide("ride001")
                );

        assertEquals(
                "A COMPLETED ride cannot be cancelled",
                exception.getMessage()
        );

        verify(rideRepository, never())
                .save(any());
    }

    @Test
    void cancelRide_shouldRejectAlreadyCancelledRide() {

        Ride ride = createRide(
                "ride001",
                RideStatus.CANCELLED
        );

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        assertThrows(
                IllegalStateException.class,
                () -> rideService.cancelRide("ride001")
        );

        verify(rideRepository, never())
                .save(any());
    }

    @Test
    void getRideById_shouldThrowExceptionWhenRideDoesNotExist() {

        when(rideRepository.findById("missingRide"))
                .thenReturn(Optional.empty());

        assertThrows(
                RideNotFoundException.class,
                () -> rideService.getRideById("missingRide")
        );
    }

    private Ride createRide(
            String id,
            RideStatus status) {

        Ride ride = new Ride();

        ride.setId(id);
        ride.setPassengerId("USER001");
        ride.setPickup("Malabe");
        ride.setDestination("Colombo");
        ride.setStatus(status);

        return ride;
    }
}