package com.loan.loan_system.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

  private final JwtUtil jwtUtil = new JwtUtil();

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(jwtUtil, "secret", "dev-secret-key-for-local-tests-only-0123456789");
    ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
  }

  @Test
  void tokenCarriesUserIdClaim() {
    UUID userId = UUID.randomUUID();
    String token = jwtUtil.generateToken("finance@loan.com", "finance", userId);

    assertEquals(userId, jwtUtil.extractUserId(token));
    assertEquals("finance@loan.com", jwtUtil.extractEmail(token));
    assertEquals("finance", jwtUtil.extractRole(token));
  }

  @Test
  void legacyTokenHasNoUserId() {
    String token = jwtUtil.generateToken("staff@loan.com", "staff");

    assertNull(jwtUtil.extractUserId(token));
  }
}
