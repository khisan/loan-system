package com.loan.loan_system.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.loan.loan_system.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
  private UUID id;
  private UUID loanId;
  private String invoiceNumber;
  private Integer installmentNumber;
  private BigDecimal principalAmount;
  private BigDecimal interestAmount;
  private BigDecimal penaltyAmount;
  private BigDecimal totalAmount;
  private Instant dueDate;
  private Instant paidAt;
  private InvoiceStatus status;
  private Instant createdAt;
  private Instant updatedAt;
}
