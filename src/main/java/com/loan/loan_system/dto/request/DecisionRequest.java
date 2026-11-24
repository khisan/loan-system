package com.loan.loan_system.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Optional body for approve/reject endpoints; note is mandatory on reject. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionRequest {
  private String note;
}
