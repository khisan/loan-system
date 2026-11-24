package com.loan.loan_system.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.loan.loan_system.enums.AuditAction;
import com.loan.loan_system.enums.BusinessNumberType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

  private UUID id;
  private BusinessNumberType entityType;
  private UUID entityId;
  private AuditAction action;
  private UUID actorId;
  private String actorEmail;
  private String actorRole;
  private Instant occurredAt;
  private Map<String, Object> before;
  private Map<String, Object> after;
  private UUID changeRequestId;
  private String description;
  private boolean selfApprovalOverride;
}
