package com.ridelink.ride.client;

import com.ridelink.ride.dto.DriverResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class DriverServiceClient {

    private final RestClient restClient;

    public DriverServiceClient(
            @Value("${driver.service.url}") String driverServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(driverServiceUrl)
                .build();
    }

    public DriverResponse getDriverById(String driverId) {

        try {
            return restClient
                    .get()
                    .uri("/api/drivers/{id}", driverId)
                    .retrieve()
                    .body(DriverResponse.class);

        } catch (RestClientResponseException exception) {

            throw new IllegalArgumentException(
                    "Driver not found with id: " + driverId
            );
        }
    }

    public boolean isDriverAvailable(String driverId) {

        DriverResponse driver =
                getDriverById(driverId);

        return driver != null
                && "AVAILABLE".equalsIgnoreCase(
                        driver.getAvailability()
                );
    }
}