package com.loan.loan_system.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.loan.loan_system.dto.response.AuditLogResponse;
import com.loan.loan_system.entity.AuditLog;
import com.loan.loan_system.enums.AuditAction;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.repository.AuditLogRepository;
import com.loan.loan_system.security.Actor;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/** Central writer/reader for the append-only audit trail. */
@Service
@RequiredArgsConstructor
public class AuditService {

  private final AuditLogRepository auditLogRepository;
  private final ObjectMapper objectMapper;
  private final JdbcTemplate jdbcTemplate;

  @Transactional
  public AuditLog record(
      BusinessNumberType entityType,
      UUID entityId,
      AuditAction action,
      Actor actor,
      String beforeJson,
      String afterJson,
      UUID changeRequestId,
      String description,
      boolean selfApprovalOverride) {

    return auditLogRepository.save(AuditLog.builder()
        .entityType(entityType)
        .entityId(entityId)
        .action(action)
        .actorId(actor.userId())
        .actorEmail(actor.email())
        .actorRole(actor.role())
        .occurredAt(Instant.now())
        .beforeJson(beforeJson)
        .afterJson(afterJson)
        .changeRequestId(changeRequestId)
        .description(description)
        .selfApprovalOverride(selfApprovalOverride)
        .build());
  }

  public String toJson(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception ex) {
      throw new IllegalStateException("Gagal membuat snapshot audit", ex);
    }
  }

  /**
   * Reads the current row as a plain map. JDBC avoids Hibernate lazy proxies and
   * keeps the audit snapshot independent of each entity's response mapper.
   */
  public Map<String, Object> snapshot(BusinessNumberType type, UUID id) {
    List<Map<String, Object>> rows = jdbcTemplate.queryForList(
        "SELECT * FROM " + type.getTable() + " WHERE id = ? AND deleted_at IS NULL", id);
    if (rows.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, type.getPrefix() + " not found with ID: " + id);
    }
    Map<String, Object> normalized = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry : rows.get(0).entrySet()) {
      Object value = entry.getValue();
      if (value instanceof Timestamp timestamp) {
        normalized.put(entry.getKey(), timestamp.toInstant().toString());
      } else if (value instanceof UUID uuid) {
        normalized.put(entry.getKey(), uuid.toString());
      } else {
        normalized.put(entry.getKey(), value);
      }
    }
    return normalized;
  }

  public Map<String, Object> toMap(String json) {
    if (json == null || json.isBlank()) {
      return Map.of();
    }
    try {
      Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
      return parsed == null ? Map.of() : parsed;
    } catch (Exception ex) {
      throw new IllegalStateException("Snapshot audit tidak valid", ex);
    }
  }

  @Transactional(readOnly = true)
  public List<AuditLogResponse> list(
      BusinessNumberType entityType,
      UUID entityId,
      UUID actorId,
      AuditAction action,
      Instant from,
      Instant to) {

    List<AuditLog> logs;
    if (entityId != null) {
      logs = auditLogRepository.findByEntityIdOrderByOccurredAtDesc(entityId);
    } else if (entityType != null && actorId != null) {
      logs = auditLogRepository.findByActorIdOrderByOccurredAtDesc(actorId).stream()
          .filter(log -> entityType == log.getEntityType())
          .toList();
    } else if (entityType != null) {
      logs = auditLogRepository.findByEntityTypeOrderByOccurredAtDesc(entityType);
    } else if (actorId != null) {
      logs = auditLogRepository.findByActorIdOrderByOccurredAtDesc(actorId);
    } else if (action != null) {
      logs = auditLogRepository.findByActionOrderByOccurredAtDesc(action);
    } else {
      logs = auditLogRepository.findAllByOrderByOccurredAtDesc();
    }

    List<AuditLogResponse> result = new ArrayList<>();
    for (AuditLog log : logs) {
      Instant occurred = log.getOccurredAt();
      if (from != null && occurred.isBefore(from)) continue;
      if (to != null && occurred.isAfter(to)) continue;
      if (action != null && log.getAction() != action) continue;
      result.add(AuditLogResponse.builder()
          .id(log.getId())
          .entityType(log.getEntityType())
          .entityId(log.getEntityId())
          .action(log.getAction())
          .actorId(log.getActorId())
          .actorEmail(log.getActorEmail())
          .actorRole(log.getActorRole())
          .occurredAt(occurred)
          .before(readMap(log.getBeforeJson()))
          .after(readMap(log.getAfterJson()))
          .changeRequestId(log.getChangeRequestId())
          .description(log.getDescription())
          .selfApprovalOverride(log.isSelfApprovalOverride())
          .build());
    }
    return result;
  }

  private Map<String, Object> readMap(String json) {
    return new LinkedHashMap<>(toMap(json));
  }
}
