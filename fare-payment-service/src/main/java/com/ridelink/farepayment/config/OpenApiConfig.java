package com.ridelink.farepayment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rideLinkFarePaymentOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("RideLink Fare & Payment Service API")
                                .version("1.0.0")
                                .description(
                                        "Backend microservice responsible for "
                                                + "fare estimation, final fare calculation, "
                                                + "simulated payments, payment status "
                                                + "and receipt generation."
                                )
                );
    }
}