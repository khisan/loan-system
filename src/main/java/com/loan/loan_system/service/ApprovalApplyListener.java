package com.loan.loan_system.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.loan.loan_system.dto.request.CollateralRequest;
import com.loan.loan_system.dto.request.CustomerRequest;
import com.loan.loan_system.dto.request.DisbursementRequest;
import com.loan.loan_system.dto.request.GuarantorRequest;
import com.loan.loan_system.dto.request.InvoiceRequest;
import com.loan.loan_system.dto.request.LoanRequest;
import com.loan.loan_system.entity.ApprovalRequest;
import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.AuditAction;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.enums.LoanStatus;
import com.loan.loan_system.event.ApprovalApprovedEvent;
import com.loan.loan_system.security.Actor;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * Applies an approved proposal through the owning service. Using a synchronous
 * event keeps ApprovalService free of dependencies on the six business services.
 */
@Component
@RequiredArgsConstructor
public class ApprovalApplyListener {

  private final ObjectMapper objectMapper;
  private final AuditService auditService;
  private final CustomerService customerService;
  private final CollateralService collateralService;
  private final LoanService loanService;
  private final GuarantorService guarantorService;
  private final InvoiceService invoiceService;
  private final DisbursementService disbursementService;

  @EventListener
  public void on(ApprovalApprovedEvent event) {
    ApprovalRequest request = event.request();
    Actor actor = event.actor();
    BusinessNumberType type = request.getEntityType();

    Map<String, Object> after = Map.of();
    String description;

    switch (request.getOperation()) {
      case CREATE -> {
        UUID createdId = applyCreate(type, request, actor);
        request.setEntityId(createdId);
        after = auditService.snapshot(type, createdId);
        description = "Proposal CREATE diterapkan";
      }
      case UPDATE -> {
        applyUpdate(type, request, actor);
        after = auditService.snapshot(type, request.getEntityId());
        description = "Proposal UPDATE diterapkan";
      }
      case DELETE -> {
        applyDelete(type, request, actor);
        description = "Proposal DELETE diterapkan";
      }
      case STATUS_CHANGE -> {
        applyStatusChange(type, request, actor);
        after = auditService.snapshot(type, request.getEntityId());
        description = "Perubahan status diterapkan";
      }
      default -> throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Operasi approval tidak dikenal");
    }

    auditService.record(
        type,
        request.getEntityId(),
        actionOf(request.getOperation()),
        actor,
        request.getBeforeJson(),
        after.isEmpty() ? null : auditService.toJson(after),
        request.getId(),
        description,
        request.isSelfApprovalOverride());
  }

  private UUID applyCreate(BusinessNumberType type, ApprovalRequest request, Actor actor) {
    return switch (type) {
      case CUSTOMER -> customerService.applyCreate(
          payload(request, type, CustomerRequest.class), request.getBusinessNumber(), actor).getId();
      case COLLATERAL -> collateralService.applyCreate(
          payload(request, type, CollateralRequest.class), request.getBusinessNumber(), actor).getId();
      case LOAN -> loanService.applyCreate(
          payload(request, type, LoanRequest.class), request.getBusinessNumber(), actor).getId();
      case GUARANTOR -> guarantorService.applyCreate(
          payload(request, type, GuarantorRequest.class), request.getBusinessNumber(), actor).getId();
      case INVOICE -> invoiceService.applyCreate(
          payload(request, type, InvoiceRequest.class), request.getBusinessNumber(), actor).getId();
      case DISBURSEMENT -> disbursementService.applyCreate(
          payload(request, type, DisbursementRequest.class), request.getBusinessNumber(), actor).getId();
    };
  }

  private void applyUpdate(BusinessNumberType type, ApprovalRequest request, Actor actor) {
    switch (type) {
      case CUSTOMER -> customerService.applyUpdate(
          request.getEntityId(), payload(request, type, CustomerRequest.class), actor);
      case COLLATERAL -> collateralService.applyUpdate(
          request.getEntityId(), payload(request, type, CollateralRequest.class), actor);
      case LOAN -> loanService.applyUpdate(
          request.getEntityId(), payload(request, type, LoanRequest.class), actor);
      case GUARANTOR -> guarantorService.applyUpdate(
          request.getEntityId(), payload(request, type, GuarantorRequest.class), actor);
      case INVOICE -> invoiceService.applyUpdate(
          request.getEntityId(), payload(request, type, InvoiceRequest.class), actor);
      case DISBURSEMENT -> disbursementService.applyUpdate(
          request.getEntityId(), payload(request, type, DisbursementRequest.class), actor);
    }
  }

  private void applyDelete(BusinessNumberType type, ApprovalRequest request, Actor actor) {
    switch (type) {
      case CUSTOMER -> customerService.applyDelete(request.getEntityId(), actor);
      case COLLATERAL -> collateralService.applyDelete(request.getEntityId(), actor);
      case LOAN -> loanService.applyDelete(request.getEntityId(), actor);
      case GUARANTOR -> guarantorService.applyDelete(request.getEntityId(), actor);
      case INVOICE -> invoiceService.applyDelete(request.getEntityId(), actor);
      case DISBURSEMENT -> disbursementService.applyDelete(request.getEntityId(), actor);
    }
  }

  private void applyStatusChange(BusinessNumberType type, ApprovalRequest request, Actor actor) {
    if (type == BusinessNumberType.LOAN) {
      LoanStatus target;
      try {
        target = LoanStatus.valueOf(request.getTargetStatus());
      } catch (IllegalArgumentException ex) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status pinjaman tidak valid");
      }
      loanService.applyStatusChange(request.getEntityId(), target, actor);
      return;
    }
    // Invoice/disbursement status lives on the row itself and is part of the payload.
    if (type == BusinessNumberType.INVOICE || type == BusinessNumberType.DISBURSEMENT) {
      applyUpdate(type, request, actor);
      return;
    }
    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST, "Entity " + type + " tidak memiliki status yang bisa diubah");
  }

  private <T> T payload(ApprovalRequest request, BusinessNumberType type, Class<T> dtoClass) {
    Map<String, Object> map = new LinkedHashMap<>(auditService.toMap(request.getPayloadJson()));
    // The reserved number is stored on the proposal, not on the request DTO.
    map.remove(type.getField());
    return objectMapper.convertValue(map, dtoClass);
  }

  private AuditAction actionOf(ApprovalOperation operation) {
    return switch (operation) {
      case CREATE -> AuditAction.CREATE;
      case UPDATE -> AuditAction.UPDATE;
      case DELETE -> AuditAction.DELETE;
      case STATUS_CHANGE -> AuditAction.STATUS_CHANGE;
    };
  }
}
