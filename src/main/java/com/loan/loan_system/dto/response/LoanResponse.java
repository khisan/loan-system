package com.loan.loan_system.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.loan.loan_system.enums.LoanStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {
  private UUID id;
  private UUID customerId;
  private BigDecimal amount;
  private Double interestRate;
  private Integer tenorMonths;
  private LoanStatus status;
  private Instant createdAt;
  private Instant updatedAt;
}
