package com.loan.loan_system.entity;

import java.time.Instant;
import java.util.UUID;

import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.ApprovalStatus;
import com.loan.loan_system.enums.BusinessNumberType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A staged mutation that only touches business data after finance approval. */
@Entity
@Table(name = "approval_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "entity_type", nullable = false, length = 32)
  private BusinessNumberType entityType;

  @Enumerated(EnumType.STRING)
  @Column(name = "operation", nullable = false, length = 32)
  private ApprovalOperation operation;

  /** Null for CREATE proposals. */
  @Column(name = "entity_id")
  private UUID entityId;

  /** Target status for STATUS_CHANGE proposals. */
  @Column(name = "target_status", length = 32)
  private String targetStatus;

  /** JSON of the proposed business fields (request DTO shape). */
  @Column(name = "payload_json", columnDefinition = "TEXT", nullable = false)
  private String payloadJson;

  /** Snapshot of the row before the proposal was submitted. */
  @Column(name = "before_json", columnDefinition = "TEXT")
  private String beforeJson;

  /** Number reserved at submit time for CREATE proposals. */
  @Column(name = "business_number", length = 32)
  private String businessNumber;

  @Column(columnDefinition = "TEXT")
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private ApprovalStatus status;

  @Column(name = "requested_by")
  private UUID requestedById;

  @Column(name = "requested_by_email", length = 255)
  private String requestedByEmail;

  @Column(name = "requested_by_role", length = 32)
  private String requestedByRole;

  @Column(name = "requested_at", nullable = false)
  private Instant requestedAt;

  @Column(name = "decided_by")
  private UUID decidedById;

  @Column(name = "decided_by_email", length = 255)
  private String decidedByEmail;

  @Column(name = "decided_at")
  private Instant decidedAt;

  @Column(name = "decision_note", columnDefinition = "TEXT")
  private String decisionNote;

  @Column(name = "self_approval_override", nullable = false)
  private boolean selfApprovalOverride;

  /** Guards against two approvers deciding the same request concurrently. */
  @Version
  private Long version;

  @jakarta.persistence.PrePersist
  protected void onCreate() {
    if (status == null) {
      status = ApprovalStatus.PENDING;
    }
    if (requestedAt == null) {
      requestedAt = Instant.now();
    }
  }
}
