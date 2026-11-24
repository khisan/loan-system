package com.loan.loan_system.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.loan.loan_system.enums.CollateralType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollateralResponse {
  private UUID id;
  private String collateralNumber;
  private UUID loanId;
  private String loanNumber;
  private String customerName;
  private CollateralType type;
  private String documentNumber;
  private BigDecimal marketValue;
  private BigDecimal pledgedValue;
  private String description;
  private Instant createdAt;
  private Instant updatedAt;
}
