package com.ridelink.farepayment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.data.mongodb.uri=mongodb://localhost:27017/fare_payment_test",
		"jwt.secret=RideLinkTestSecretKeyForSecurityTests2026"
})
class FarePaymentServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
