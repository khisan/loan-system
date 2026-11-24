package com.loan.loan_system.entity;

import java.time.Instant;
import java.util.UUID;

import com.loan.loan_system.enums.AuditAction;
import com.loan.loan_system.enums.BusinessNumberType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Append-only record of who did what, when, and with which values. */
@Entity
@Table(name = "audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "entity_type", length = 32)
  private BusinessNumberType entityType;

  @Column(name = "entity_id")
  private UUID entityId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private AuditAction action;

  @Column(name = "actor_id")
  private UUID actorId;

  @Column(name = "actor_email", length = 255)
  private String actorEmail;

  @Column(name = "actor_role", length = 32)
  private String actorRole;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "before_json", columnDefinition = "TEXT")
  private String beforeJson;

  @Column(name = "after_json", columnDefinition = "TEXT")
  private String afterJson;

  @Column(name = "change_request_id")
  private UUID changeRequestId;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "self_approval_override", nullable = false)
  private boolean selfApprovalOverride;

  @jakarta.persistence.PrePersist
  protected void onCreate() {
    if (occurredAt == null) {
      occurredAt = Instant.now();
    }
  }
}
