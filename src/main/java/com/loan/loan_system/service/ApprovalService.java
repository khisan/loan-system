package com.loan.loan_system.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.loan.loan_system.dto.request.ChangeRequest;
import com.loan.loan_system.dto.response.ApprovalRequestResponse;
import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.ApprovalStatus;
import com.loan.loan_system.enums.AuditAction;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.entity.ApprovalRequest;
import com.loan.loan_system.event.ApprovalApprovedEvent;
import com.loan.loan_system.repository.ApprovalRequestRepository;
import com.loan.loan_system.security.Actor;
import com.loan.loan_system.security.ActorContext;

import lombok.RequiredArgsConstructor;

/** Creates, decides and lists staged change requests. */
@Service
@RequiredArgsConstructor
public class ApprovalService {

  private final ApprovalRequestRepository approvalRequestRepository;
  private final BusinessNumberService businessNumberService;
  private final ActorContext actorContext;
  private final AuditService auditService;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public ApprovalRequestResponse submit(ChangeRequest request) {
    Actor actor = actorContext.current();
    // Finance is a read-only approver; it never submits business changes.
    if ("finance".equals(normalize(actor.role()))) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Finance hanya bisa menyetujui proposal, bukan mengirim perubahan");
    }
    BusinessNumberType type = request.getEntityType();
    ApprovalOperation operation = request.getOperation();

    if (type == null || operation == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entity type dan operasi wajib diisi");
    }
    if (operation != ApprovalOperation.CREATE && request.getEntityId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entity ID wajib diisi untuk operasi ini");
    }
    if (operation == ApprovalOperation.STATUS_CHANGE
        && (request.getTargetStatus() == null || request.getTargetStatus().isBlank())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target status wajib diisi");
    }

    Map<String, Object> payload = request.getPayload() == null
        ? new LinkedHashMap<>()
        : new LinkedHashMap<>(request.getPayload());
    Map<String, Object> before = Map.of();
    String businessNumber = null;

    if (operation == ApprovalOperation.CREATE) {
      businessNumber = businessNumberService.next(type, Instant.now());
      // Numbers are reserved at submit time so the proposal has a display reference.
      payload.put(type.getField(), businessNumber);
    } else {
      before = auditService.snapshot(type, request.getEntityId());
      // Carry the existing business number into the proposal for human display.
      // The listener strips this camelCase key before mapping onto the request DTO.
      Object existingNumber = before.get(type.getColumn());
      if (existingNumber != null) {
        businessNumber = existingNumber.toString();
        payload.put(type.getField(), businessNumber);
      }
    }

    String beforeJson = before.isEmpty() ? null : auditService.toJson(before);
    ApprovalRequest proposal = approvalRequestRepository.save(ApprovalRequest.builder()
        .entityType(type)
        .operation(operation)
        .entityId(request.getEntityId())
        .targetStatus(request.getTargetStatus())
        .payloadJson(auditService.toJson(payload))
        .beforeJson(beforeJson)
        .businessNumber(businessNumber)
        .reason(request.getReason())
        .status(ApprovalStatus.PENDING)
        .requestedById(actor.userId())
        .requestedByEmail(actor.email())
        .requestedByRole(actor.role())
        .requestedAt(Instant.now())
        .selfApprovalOverride(false)
        .build());

    auditService.record(
        type,
        proposal.getEntityId(),
        AuditAction.SUBMIT,
        actor,
        beforeJson,
        auditService.toJson(payload),
        proposal.getId(),
        "Proposal " + operation + " dikirim untuk " + type.getPrefix(),
        false);

    return toResponse(proposal);
  }

  @Transactional
  public ApprovalRequestResponse approve(UUID id, String note) {
    Actor actor = actorContext.current();
    requireDecisionRole(actor);

    ApprovalRequest proposal = findById(id);
    requirePending(proposal);

    boolean override = isSelfApproval(actor, proposal);
    if (override) {
      if (!"admin".equals(normalize(actor.role()))) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "Finance tidak boleh menyetujui proposal sendiri");
      }
      if (note == null || note.isBlank()) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Alasan wajib diisi saat admin menyetujui proposal sendiri");
      }
    }

    proposal.setStatus(ApprovalStatus.APPROVED);
    proposal.setDecidedById(actor.userId());
    proposal.setDecidedByEmail(actor.email());
    proposal.setDecidedAt(Instant.now());
    proposal.setDecisionNote(note);
    proposal.setSelfApprovalOverride(override);
    ApprovalRequest saved = approvalRequestRepository.saveAndFlush(proposal);

    // The listener applies the staged payload in this same transaction.
    eventPublisher.publishEvent(new ApprovalApprovedEvent(saved, actor));

    auditService.record(
        saved.getEntityType(),
        saved.getEntityId(),
        AuditAction.APPROVE,
        actor,
        null,
        null,
        saved.getId(),
        note == null || note.isBlank() ? "Proposal disetujui" : note,
        override);
    return toResponse(saved);
  }

  @Transactional
  public ApprovalRequestResponse reject(UUID id, String note) {
    Actor actor = actorContext.current();
    requireDecisionRole(actor);

    if (note == null || note.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Alasan penolakan wajib diisi");
    }
    ApprovalRequest proposal = findById(id);
    requirePending(proposal);
    proposal.setStatus(ApprovalStatus.REJECTED);
    proposal.setDecidedById(actor.userId());
    proposal.setDecidedByEmail(actor.email());
    proposal.setDecidedAt(Instant.now());
    proposal.setDecisionNote(note);
    proposal.setSelfApprovalOverride(isSelfApproval(actor, proposal));
    ApprovalRequest saved = approvalRequestRepository.save(proposal);

    auditService.record(
        saved.getEntityType(),
        saved.getEntityId(),
        AuditAction.REJECT,
        actor,
        null,
        null,
        saved.getId(),
        note,
        saved.isSelfApprovalOverride());
    return toResponse(saved);
  }

  @Transactional
  public ApprovalRequestResponse cancel(UUID id) {
    Actor actor = actorContext.current();
    ApprovalRequest proposal = findById(id);
    requirePending(proposal);

    boolean owner = actor.userId() != null && actor.userId().equals(proposal.getRequestedById());
    if (!owner && !"admin".equals(normalize(actor.role()))) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Hanya pembuat proposal atau admin yang bisa membatalkan");
    }
    proposal.setStatus(ApprovalStatus.CANCELLED);
    proposal.setDecidedById(actor.userId());
    proposal.setDecidedByEmail(actor.email());
    proposal.setDecidedAt(Instant.now());
    ApprovalRequest saved = approvalRequestRepository.save(proposal);

    auditService.record(
        saved.getEntityType(),
        saved.getEntityId(),
        AuditAction.CANCEL,
        actor,
        null,
        null,
        saved.getId(),
        "Proposal dibatalkan",
        false);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<ApprovalRequestResponse> list(ApprovalStatus status, BusinessNumberType entityType) {
    List<ApprovalRequest> proposals;
    if (status != null && entityType != null) {
      proposals = approvalRequestRepository
          .findByStatusAndEntityTypeOrderByRequestedAtDesc(status, entityType);
    } else if (status != null) {
      proposals = approvalRequestRepository.findByStatusOrderByRequestedAtDesc(status);
    } else if (entityType != null) {
      proposals = approvalRequestRepository.findByEntityTypeOrderByRequestedAtDesc(entityType);
    } else {
      proposals = approvalRequestRepository.findAllByOrderByRequestedAtDesc();
    }
    List<ApprovalRequestResponse> result = new ArrayList<>();
    for (ApprovalRequest proposal : proposals) {
      result.add(toResponse(proposal));
    }
    return result;
  }

  @Transactional(readOnly = true)
  public ApprovalRequestResponse get(UUID id) {
    return toResponse(findById(id));
  }

  private ApprovalRequest findById(UUID id) {
    return approvalRequestRepository.findById(id).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval request not found with ID: " + id));
  }

  private void requirePending(ApprovalRequest proposal) {
    if (proposal.getStatus() != ApprovalStatus.PENDING) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Proposal sudah " + proposal.getStatus() + " dan tidak bisa diubah lagi");
    }
  }

  private void requireDecisionRole(Actor actor) {
    String role = normalize(actor.role());
    if (!"admin".equals(role) && !"finance".equals(role)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Hanya role finance atau admin yang bisa memutuskan approval");
    }
  }

  private boolean isSelfApproval(Actor actor, ApprovalRequest proposal) {
    return actor.userId() != null && actor.userId().equals(proposal.getRequestedById());
  }

  private String normalize(String role) {
    return role == null ? "" : role.trim().toLowerCase(Locale.ROOT);
  }

  private ApprovalRequestResponse toResponse(ApprovalRequest proposal) {
    return ApprovalRequestResponse.builder()
        .id(proposal.getId())
        .entityType(proposal.getEntityType())
        .operation(proposal.getOperation())
        .entityId(proposal.getEntityId())
        .businessNumber(proposal.getBusinessNumber())
        .targetStatus(proposal.getTargetStatus())
        .status(proposal.getStatus())
        .reason(proposal.getReason())
        .requestedById(proposal.getRequestedById())
        .requestedByEmail(proposal.getRequestedByEmail())
        .requestedByRole(proposal.getRequestedByRole())
        .requestedAt(proposal.getRequestedAt())
        .decidedById(proposal.getDecidedById())
        .decidedByEmail(proposal.getDecidedByEmail())
        .decidedAt(proposal.getDecidedAt())
        .decisionNote(proposal.getDecisionNote())
        .selfApprovalOverride(proposal.isSelfApprovalOverride())
        .payload(auditService.toMap(proposal.getPayloadJson()))
        .before(auditService.toMap(proposal.getBeforeJson()))
        .build();
  }
}
