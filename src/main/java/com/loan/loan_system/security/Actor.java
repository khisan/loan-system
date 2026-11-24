package com.loan.loan_system.security;

import java.io.Serializable;
import java.util.UUID;

/**
 * Identity attached to the security context. Used by the approval and audit
 * services so every mutation records who triggered it.
 */
public record Actor(UUID userId, String email, String role) implements Serializable {

  public static final Actor SYSTEM = new Actor(null, "system", "system");

  public boolean isSystem() {
    return "system".equals(role);
  }
}
