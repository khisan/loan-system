package com.loan.loan_system.dto.response;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder 
@NoArgsConstructor
@AllArgsConstructor 
public class UserResponse {
  private UUID id;
  private String name;
  private String email;
  private String password;
  private String role;
  private Instant createdAt;
  private Instant updatedAt;
}
