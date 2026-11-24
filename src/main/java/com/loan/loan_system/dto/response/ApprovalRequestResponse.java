package com.loan.loan_system.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.ApprovalStatus;
import com.loan.loan_system.enums.BusinessNumberType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestResponse {

  private UUID id;
  private BusinessNumberType entityType;
  private ApprovalOperation operation;
  private UUID entityId;
  private String businessNumber;
  private String targetStatus;
  private ApprovalStatus status;
  private String reason;
  private UUID requestedById;
  private String requestedByEmail;
  private String requestedByRole;
  private Instant requestedAt;
  private UUID decidedById;
  private String decidedByEmail;
  private Instant decidedAt;
  private String decisionNote;
  private boolean selfApprovalOverride;
  private Map<String, Object> payload;
  private Map<String, Object> before;
}
