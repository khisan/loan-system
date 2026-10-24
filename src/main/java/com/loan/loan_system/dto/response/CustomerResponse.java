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
public class CustomerResponse {
  private UUID id;
  private String fullName;
  private String identityNumber;
  private String email;
  private String phoneNumber;
  private String address;
  private Instant createdAt;
  private Instant updatedAt;
}
