package com.loan.loan_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.loan.loan_system.entity.ApprovalRequest;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

/** Sends approval notifications. Failures are logged so approvals never roll back. */
@Service
@RequiredArgsConstructor
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;

  @Value("${app.mail.enabled:false}")
  private boolean enabled;

  @Value("${app.mail.from:loan-system@localhost}")
  private String from;

  @Value("${app.frontend-url:http://localhost:5173}")
  private String frontendUrl;

  public void sendApprovalSubmitted(ApprovalRequest request, String recipient) {
    String reference = reference(request);
    String subject = "Persetujuan baru menunggu: " + reference;
    String text = String.join("\n",
        "Ada proposal baru yang menunggu persetujuan.",
        "No. Bisnis: " + reference,
        "Jenis data: " + request.getEntityType(),
        "Operasi: " + request.getOperation(),
        "Diajukan oleh: " + valueOrDash(request.getRequestedByEmail()),
        "Alasan: " + valueOrDash(request.getReason()),
        "Lihat: " + frontendUrl + "/approvals?status=PENDING");
    String html = "<p>Ada proposal baru yang menunggu persetujuan.</p>"
        + "<ul>"
        + "<li>No. Bisnis: <strong>" + escape(reference) + "</strong></li>"
        + "<li>Jenis data: " + escape(request.getEntityType().name()) + "</li>"
        + "<li>Operasi: " + escape(request.getOperation().name()) + "</li>"
        + "<li>Diajukan oleh: " + escape(valueOrDash(request.getRequestedByEmail())) + "</li>"
        + "<li>Alasan: " + escape(valueOrDash(request.getReason())) + "</li>"
        + "</ul>"
        + "<p><a href=\"" + escape(frontendUrl) + "/approvals?status=PENDING\">Buka halaman Persetujuan</a></p>";
    send(recipient, subject, text, html);
  }

  public void sendApprovalDecision(
      ApprovalRequest request, String note, String decidedByEmail, String recipient) {
    String reference = reference(request);
    String subject = "Proposal " + reference + " " + statusLabel(request);
    String text = String.join("\n",
        "Proposal kamu sudah " + statusLabel(request) + ".",
        "No. Bisnis: " + reference,
        "Jenis data: " + request.getEntityType(),
        "Diputuskan oleh: " + valueOrDash(decidedByEmail),
        "Catatan: " + valueOrDash(note),
        "Lihat: " + frontendUrl + "/approvals?status=" + request.getStatus());
    String html = "<p>Proposal kamu sudah <strong>" + escape(statusLabel(request)) + "</strong>.</p>"
        + "<ul>"
        + "<li>No. Bisnis: <strong>" + escape(reference) + "</strong></li>"
        + "<li>Jenis data: " + escape(request.getEntityType().name()) + "</li>"
        + "<li>Diputuskan oleh: " + escape(valueOrDash(decidedByEmail)) + "</li>"
        + "<li>Catatan: " + escape(valueOrDash(note)) + "</li>"
        + "</ul>"
        + "<p><a href=\"" + escape(frontendUrl) + "/approvals?status=" + escape(request.getStatus().name())
        + "\">Lihat proposal</a></p>";
    send(recipient, subject, text, html);
  }

  private void send(String recipient, String subject, String text, String html) {
    if (!enabled) {
      log.info("[MAIL DISABLED] To: {} | Subject: {} | Body: {}", recipient, subject, text);
      return;
    }
    if (recipient == null || recipient.isBlank()) {
      log.warn("Email notifikasi dilewati: penerima kosong (subjek: {})", subject);
      return;
    }
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(from);
      helper.setTo(recipient);
      helper.setSubject(subject);
      helper.setText(text, html);
      mailSender.send(message);
    } catch (Exception ex) {
      log.error("Gagal mengirim email ke {}: {}", recipient, ex.getMessage());
    }
  }

  private String reference(ApprovalRequest request) {
    return request.getBusinessNumber() != null && !request.getBusinessNumber().isBlank()
        ? request.getBusinessNumber()
        : String.valueOf(request.getId());
  }

  private String statusLabel(ApprovalRequest request) {
    if (request.getStatus() == null) return "diputuskan";
    return switch (request.getStatus()) {
      case APPROVED -> "disetujui";
      case REJECTED -> "ditolak";
      case CANCELLED -> "dibatalkan";
      default -> "diputuskan";
    };
  }

  private String valueOrDash(String value) {
    return value == null || value.isBlank() ? "—" : value;
  }

  private String escape(String value) {
    if (value == null) return "";
    return value.replace("&", "&amp;").replace("<", "&lt;")
        .replace(">", "&gt;").replace("\"", "&quot;");
  }
}
