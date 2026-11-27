package com.loan.loan_system.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.loan.loan_system.entity.ApprovalRequest;
import com.loan.loan_system.entity.User;
import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.ApprovalStatus;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.event.ApprovalDecisionEvent;
import com.loan.loan_system.event.ApprovalSubmittedEvent;
import com.loan.loan_system.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ApprovalNotificationListenerTest {

  @Mock private UserRepository userRepository;
  @Mock private EmailService emailService;
  @InjectMocks private ApprovalNotificationListener listener;

  private ApprovalRequest request(ApprovalStatus status) {
    return ApprovalRequest.builder()
        .id(UUID.randomUUID())
        .entityType(BusinessNumberType.LOAN)
        .operation(ApprovalOperation.STATUS_CHANGE)
        .businessNumber("LN-2026-0001")
        .status(status)
        .requestedByEmail("staff@loan.com")
        .build();
  }

  @Test
  void submittedNotifiesAdminAndFinance() {
    ApprovalRequest request = request(ApprovalStatus.PENDING);
    when(userRepository.findAllByRoleIn(List.of("admin", "finance"))).thenReturn(List.of(
        User.builder().email("admin@loan.com").role("admin").build(),
        User.builder().email("finance@loan.com").role("finance").build(),
        User.builder().email("admin@loan.com").role("admin").build()));

    listener.onSubmitted(new ApprovalSubmittedEvent(request));

    verify(emailService).sendApprovalSubmitted(request, "admin@loan.com");
    verify(emailService).sendApprovalSubmitted(request, "finance@loan.com");
  }

  @Test
  void oneFailedEmailDoesNotBlockTheOtherRecipient() {
    ApprovalRequest request = request(ApprovalStatus.PENDING);
    when(userRepository.findAllByRoleIn(List.of("admin", "finance"))).thenReturn(List.of(
        User.builder().email("admin@loan.com").role("admin").build(),
        User.builder().email("finance@loan.com").role("finance").build()));
    doThrow(new RuntimeException("SMTP down"))
        .when(emailService).sendApprovalSubmitted(any(), eq("admin@loan.com"));

    assertDoesNotThrow(() -> listener.onSubmitted(new ApprovalSubmittedEvent(request)));

    verify(emailService).sendApprovalSubmitted(request, "finance@loan.com");
  }

  @Test
  void decisionNotifiesRequester() {
    ApprovalRequest request = request(ApprovalStatus.REJECTED);

    listener.onDecided(new ApprovalDecisionEvent(request, "Alasan lengkap", "finance@loan.com"));

    verify(emailService).sendApprovalDecision(
        request, "Alasan lengkap", "finance@loan.com", "staff@loan.com");
  }
}
