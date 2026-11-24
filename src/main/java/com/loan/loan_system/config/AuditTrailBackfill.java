package com.loan.loan_system.config;

import java.util.List;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.loan.loan_system.enums.AuditAction;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.security.Actor;
import com.loan.loan_system.service.AuditService;

import lombok.RequiredArgsConstructor;

/**
 * Adds audit indexes and records existing rows as SYSTEM-created so the trail
 * has no silent gaps. Only runs when no CREATE audit entry exists yet.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class AuditTrailBackfill implements ApplicationRunner {
  private final JdbcTemplate jdbcTemplate;
  private final AuditService auditService;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_log (entity_type, entity_id, occurred_at)");
    jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_audit_actor ON audit_log (actor_id, occurred_at)");
    jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_approval_status ON approval_requests (status, requested_at)");

    Long existing = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM audit_log WHERE action = ?", Long.class, AuditAction.CREATE.name());
    if (existing != null && existing > 0) {
      return;
    }

    for (BusinessNumberType type : BusinessNumberType.values()) {
      List<UUID> ids = jdbcTemplate.queryForList(
          "SELECT id FROM " + type.getTable() + " WHERE deleted_at IS NULL", UUID.class);
      for (UUID id : ids) {
        auditService.record(
            type,
            id,
            AuditAction.CREATE,
            Actor.SYSTEM,
            null,
            auditService.toJson(auditService.snapshot(type, id)),
            null,
            "Data lama sebelum audit trail diaktifkan",
            false);
      }
    }
  }
}
