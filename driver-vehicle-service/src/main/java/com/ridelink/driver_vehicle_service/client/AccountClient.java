package com.ridelink.driver_vehicle_service.client;

import com.ridelink.driver_vehicle_service.dto.AccountResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AccountClient {

    private final RestClient restClient;

    public AccountClient(
            @Value("${account.service.url}")
            String accountServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(accountServiceUrl)
                .build();
    }

    public AccountResponse getAccountById(
            String accountId,
            String authorizationHeader) {

        return restClient
                .get()
                .uri(
                        "/api/accounts/{id}",
                        accountId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .body(AccountResponse.class);
    }
}