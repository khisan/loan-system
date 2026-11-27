package com.loan.loan_system;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Runners would otherwise seed/backfill the real dev database during tests.
@SpringBootTest(properties = "app.runners.enabled=false")
class LoanSystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
