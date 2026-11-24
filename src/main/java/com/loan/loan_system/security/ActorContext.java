package com.loan.loan_system.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Resolves the current actor without coupling services to Spring Security. */
@Component
public class ActorContext {

  public Actor current() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || authentication.getPrincipal() == null) {
      return Actor.SYSTEM;
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof Actor actor) {
      return actor;
    }
    // Legacy/anonymous principal: keep the email, but there is no trustworthy user id.
    if (principal instanceof String email && !"anonymousUser".equals(email)) {
      return new Actor(null, email, "unknown");
    }
    return Actor.SYSTEM;
  }
}
