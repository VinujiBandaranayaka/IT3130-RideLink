package com.ridelink.farepayment.client;

import com.ridelink.farepayment.dto.RideDto;
import com.ridelink.farepayment.exception.ServiceUnavailableException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RideClient {

    private final RestClient restClient;


    // Constructor injection
    public RideClient(
            RestClient.Builder restClientBuilder,
            @Value("${ride.service.url}") String rideServiceUrl) {

        this.restClient = restClientBuilder
                .baseUrl(rideServiceUrl)
                .build();
    }


    // ========================================
    // GET RIDE BY ID
    // ========================================

    public RideDto getRideById(String rideId) {

        if (rideId == null || rideId.isBlank()) {
            throw new IllegalArgumentException(
                    "Ride ID cannot be empty"
            );
        }

        try {

            RideDto ride = restClient
                    .get()
                    .uri("/api/rides/{rideId}", rideId)
                    .retrieve()
                    .body(RideDto.class);

            if (ride == null) {
                throw new ServiceUnavailableException(
                        "Ride Management Service returned an empty response"
                );
            }

            return ride;

        } catch (RestClientResponseException ex) {

            throw new ServiceUnavailableException(
                    "Ride Management Service request failed",
                    ex
            );

        } catch (ResourceAccessException ex) {

            throw new ServiceUnavailableException(
                    "Ride Management Service is unavailable",
                    ex
            );
        }
    }
}