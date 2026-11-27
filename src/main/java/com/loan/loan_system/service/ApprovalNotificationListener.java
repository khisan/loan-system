package com.loan.loan_system.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.loan.loan_system.entity.User;
import com.loan.loan_system.event.ApprovalDecisionEvent;
import com.loan.loan_system.event.ApprovalSubmittedEvent;
import com.loan.loan_system.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/** Emails approval notifications only after the originating transaction commits. */
@Component
@RequiredArgsConstructor
public class ApprovalNotificationListener {

  private static final Logger log = LoggerFactory.getLogger(ApprovalNotificationListener.class);
  private static final List<String> APPROVER_ROLES = List.of("admin", "finance");

  private final UserRepository userRepository;
  private final EmailService emailService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSubmitted(ApprovalSubmittedEvent event) {
    try {
      List<String> recipients = userRepository.findAllByRoleIn(APPROVER_ROLES).stream()
          .map(User::getEmail)
          .filter(email -> email != null && !email.isBlank())
          .distinct()
          .toList();
      for (String recipient : recipients) {
        try {
          emailService.sendApprovalSubmitted(event.request(), recipient);
        } catch (Exception ex) {
          log.error("Gagal mengirim email ke {}: {}", recipient, ex.getMessage());
        }
      }
    } catch (Exception ex) {
      log.error("Gagal menyiapkan email notifikasi proposal baru: {}", ex.getMessage());
    }
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onDecided(ApprovalDecisionEvent event) {
    try {
      String recipient = event.request().getRequestedByEmail();
      if (recipient != null && !recipient.isBlank()) {
        emailService.sendApprovalDecision(
            event.request(), event.note(), event.decidedByEmail(), recipient);
      }
    } catch (Exception ex) {
      log.error("Gagal menyiapkan email notifikasi keputusan: {}", ex.getMessage());
    }
  }
}
