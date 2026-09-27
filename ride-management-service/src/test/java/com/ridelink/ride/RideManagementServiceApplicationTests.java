package com.ridelink.ride;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "JWT_SECRET=RideLinkTestSecretKeyForJWT2026SecureEnough12345678901234567890",
        "MONGODB_URI=mongodb://localhost:27017/ridelink_test_db"
})
class RideManagementServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}