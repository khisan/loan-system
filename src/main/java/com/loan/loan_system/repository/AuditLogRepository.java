package com.loan.loan_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loan.loan_system.entity.AuditLog;
import com.loan.loan_system.enums.AuditAction;
import com.loan.loan_system.enums.BusinessNumberType;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

  List<AuditLog> findAllByOrderByOccurredAtDesc();

  List<AuditLog> findByEntityTypeOrderByOccurredAtDesc(BusinessNumberType entityType);

  List<AuditLog> findByEntityIdOrderByOccurredAtDesc(UUID entityId);

  List<AuditLog> findByActorIdOrderByOccurredAtDesc(UUID actorId);

  List<AuditLog> findByActionOrderByOccurredAtDesc(AuditAction action);
}
