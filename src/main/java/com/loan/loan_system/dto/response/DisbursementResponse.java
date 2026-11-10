package com.loan.loan_system.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.loan.loan_system.enums.DisbursementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementResponse {
  private UUID id;
  private UUID loanId;
  private String disbursementNumber;
  private BigDecimal amount;
  private String bankName;
  private String bankAccountNumber;
  private String bankAccountName;
  private DisbursementStatus status;
  private Instant disbursedAt;
  private Instant createdAt;
  private Instant updatedAt;
}
