package com.loan.loan_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.loan.loan_system.dto.request.ChangeRequest;
import com.loan.loan_system.dto.response.ApprovalRequestResponse;
import com.loan.loan_system.entity.ApprovalRequest;
import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.ApprovalStatus;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.event.ApprovalApprovedEvent;
import com.loan.loan_system.repository.ApprovalRequestRepository;
import com.loan.loan_system.security.Actor;
import com.loan.loan_system.security.ActorContext;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

  @Mock private ApprovalRequestRepository approvalRequestRepository;
  @Mock private BusinessNumberService businessNumberService;
  @Mock private ActorContext actorContext;
  @Mock private AuditService auditService;
  @Mock private ApplicationEventPublisher eventPublisher;

  private ApprovalService approvalService;

  private final Actor staff = new Actor(UUID.randomUUID(), "staff@loan.com", "staff");
  private final Actor finance = new Actor(UUID.randomUUID(), "finance@loan.com", "finance");
  private final Actor admin = new Actor(UUID.randomUUID(), "admin@loan.com", "admin");

  @BeforeEach
  void setUp() {
    approvalService = new ApprovalService(
        approvalRequestRepository,
        businessNumberService,
        actorContext,
        auditService,
        eventPublisher);
  }

  @Test
  void submitCreateReservesBusinessNumber() {
    when(actorContext.current()).thenReturn(staff);
    when(businessNumberService.next(any(), any())).thenReturn("LN-2026-0001");
    when(auditService.toJson(any())).thenAnswer(invocation -> invocation.getArgument(0).toString());
    when(approvalRequestRepository.save(any())).thenAnswer(invocation -> {
      ApprovalRequest proposal = invocation.getArgument(0);
      proposal.setId(UUID.randomUUID());
      return proposal;
    });

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("amount", 500000);
    ApprovalRequestResponse response = approvalService.submit(new ChangeRequest(
        BusinessNumberType.LOAN, ApprovalOperation.CREATE, null, payload, null, "Pengajuan"));

    assertEquals("LN-2026-0001", response.getBusinessNumber());
    assertEquals(ApprovalStatus.PENDING, response.getStatus());
    ArgumentCaptor<ApprovalRequest> captor = ArgumentCaptor.forClass(ApprovalRequest.class);
    verify(approvalRequestRepository).save(captor.capture());
    assertTrue(captor.getValue().getPayloadJson().contains("LN-2026-0001"));
    assertEquals(staff.userId(), captor.getValue().getRequestedById());
  }

  @Test
  void financeCannotApproveOwnProposal() {
    Actor requester = new Actor(finance.userId(), finance.email(), finance.role());
    when(actorContext.current()).thenReturn(requester);
    ApprovalRequest proposal = pendingProposal(requester.userId(), "finance");
    when(approvalRequestRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));

    ResponseStatusException error = assertThrows(
        ResponseStatusException.class, () -> approvalService.approve(proposal.getId(), null));

    assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void adminCanOverrideOwnProposalWithNote() {
    Actor requester = new Actor(admin.userId(), admin.email(), admin.role());
    when(actorContext.current()).thenReturn(requester);
    ApprovalRequest proposal = pendingProposal(requester.userId(), "admin");
    when(approvalRequestRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
    when(approvalRequestRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ApprovalRequestResponse response = approvalService.approve(proposal.getId(), "Override audit");

    assertEquals(ApprovalStatus.APPROVED, response.getStatus());
    assertTrue(response.isSelfApprovalOverride());
    verify(eventPublisher).publishEvent(any(ApprovalApprovedEvent.class));
  }

  @Test
  void nonDecisionRoleCannotApprove() {
    when(actorContext.current()).thenReturn(staff);

    ResponseStatusException error = assertThrows(
        ResponseStatusException.class,
        () -> approvalService.approve(UUID.randomUUID(), null));

    assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
  }

  @Test
  void rejectRequiresReasonAndClosesProposal() {
    when(actorContext.current()).thenReturn(finance);
    ApprovalRequest proposal = pendingProposal(UUID.randomUUID(), "staff");
    when(approvalRequestRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
    when(approvalRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    assertThrows(
        ResponseStatusException.class, () -> approvalService.reject(proposal.getId(), "  "));

    ApprovalRequestResponse response = approvalService.reject(proposal.getId(), "Data tidak sesuai");
    assertEquals(ApprovalStatus.REJECTED, response.getStatus());
    assertEquals("Data tidak sesuai", response.getDecisionNote());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void decidedProposalCannotBeApprovedAgain() {
    when(actorContext.current()).thenReturn(finance);
    ApprovalRequest proposal = pendingProposal(UUID.randomUUID(), "staff");
    proposal.setStatus(ApprovalStatus.REJECTED);
    when(approvalRequestRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));

    ResponseStatusException error = assertThrows(
        ResponseStatusException.class, () -> approvalService.approve(proposal.getId(), null));

    assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
  }

  private ApprovalRequest pendingProposal(UUID requesterId, String requesterRole) {
    return ApprovalRequest.builder()
        .id(UUID.randomUUID())
        .entityType(BusinessNumberType.LOAN)
        .operation(ApprovalOperation.STATUS_CHANGE)
        .entityId(UUID.randomUUID())
        .targetStatus("APPROVED")
        .payloadJson("{}")
        .status(ApprovalStatus.PENDING)
        .requestedById(requesterId)
        .requestedByEmail("requester@loan.com")
        .requestedByRole(requesterRole)
        .build();
  }
}
