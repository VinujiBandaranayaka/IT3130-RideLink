package com.ridelink.ride.controller;

import com.ridelink.ride.service.RideService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=0123456789012345678901234567890123456789012345678901234567890123",
        "spring.mongodb.uri=mongodb://localhost:27017/ridelink_test"
})
@AutoConfigureMockMvc
class RideSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RideService rideService;

    @Test
    void getRideWithoutJwtShouldReturn401() throws Exception {

        mockMvc.perform(
                get("/api/rides/test-ride-id")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void passengerShouldNotAssignDriver() throws Exception {

        mockMvc.perform(
                put("/api/rides/test-ride-id/assign")
                        .param("driverId", "test-driver-id")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_PASSENGER")
                        ))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldBeAllowedToAssignDriver() throws Exception {

        mockMvc.perform(
                put("/api/rides/test-ride-id/assign")
                        .param("driverId", "test-driver-id")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
        )
        .andExpect(status().isOk());
    }

    @Test
    void driverShouldBeAllowedToStartRide() throws Exception {

        mockMvc.perform(
                put("/api/rides/test-ride-id/start")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_DRIVER")
                        ))
        )
        .andExpect(status().isOk());
    }
}
