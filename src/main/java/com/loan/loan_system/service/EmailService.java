package com.loan.loan_system.service;

import java.util.List;

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

  private record Detail(String label, String value) {}

  public void sendApprovalSubmitted(ApprovalRequest request, String recipient) {
    String reference = reference(request);
    String url = frontendUrl + "/approvals?status=PENDING";
    String subject = "Persetujuan baru menunggu: " + reference;
    String text = String.join("\n",
        "Ada proposal baru yang menunggu persetujuan.",
        "No. Bisnis: " + reference,
        "Jenis data: " + request.getEntityType(),
        "Operasi: " + request.getOperation(),
        "Diajukan oleh: " + valueOrDash(request.getRequestedByEmail()),
        "Alasan: " + valueOrDash(request.getReason()),
        "Lihat: " + url);
    String html = dashboardEmail(
        "Persetujuan",
        "Persetujuan baru menunggu",
        "Ada proposal baru yang menunggu tindakan kamu.",
        "PENDING",
        List.of(
            detail("No. Bisnis", reference),
            detail("Jenis data", request.getEntityType().name()),
            detail("Operasi", request.getOperation().name()),
            detail("Diajukan oleh", request.getRequestedByEmail()),
            detail("Alasan", request.getReason())),
        "Buka halaman Persetujuan",
        url);
    send(recipient, subject, text, html);
  }

  public void sendApprovalDecision(
      ApprovalRequest request, String note, String decidedByEmail, String recipient) {
    String reference = reference(request);
    String status = request.getStatus() == null ? "DIPUTUSKAN" : request.getStatus().name();
    String url = frontendUrl + "/approvals?status=" + status;
    String subject = "Proposal " + reference + " " + statusLabel(request);
    String text = String.join("\n",
        "Proposal kamu sudah " + statusLabel(request) + ".",
        "No. Bisnis: " + reference,
        "Jenis data: " + request.getEntityType(),
        "Operasi: " + request.getOperation(),
        "Diputuskan oleh: " + valueOrDash(decidedByEmail),
        "Catatan: " + valueOrDash(note),
        "Lihat: " + url);
    String html = dashboardEmail(
        "Persetujuan",
        "Proposal " + statusLabel(request),
        "Proposal kamu sudah " + statusLabel(request) + ".",
        status,
        List.of(
            detail("No. Bisnis", reference),
            detail("Jenis data", request.getEntityType().name()),
            detail("Operasi", request.getOperation().name()),
            detail("Diputuskan oleh", decidedByEmail),
            detail("Catatan", note)),
        "Lihat proposal",
        url);
    send(recipient, subject, text, html);
  }

  /**
   * HTML email modeled after the dashboard UI: stone surfaces, blue accent,
   * status chip, detail table and primary button. All styles are inline so
   * email clients render them without external stylesheets.
   */
  private String dashboardEmail(
      String eyebrow,
      String title,
      String description,
      String status,
      List<Detail> details,
      String ctaLabel,
      String ctaUrl) {
    String template = """
        <!doctype html>
        <html lang="id">
        <head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
        <body style="margin:0;padding:0;background-color:#f5f5f4;">
          <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" style="padding:24px 12px;">
            <tr><td align="center">
              <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" style="max-width:600px;background-color:#ffffff;border:1px solid #e7e5e4;border-radius:12px;font-family:ui-sans-serif,system-ui,-apple-system,'Segoe UI',sans-serif;">
                <tr><td style="padding:18px 24px;border-bottom:1px solid #e7e5e4;">
                  <table role="presentation" cellpadding="0" cellspacing="0" border="0">
                    <tr>
                      <td width="40" height="40" align="center" valign="middle" style="width:40px;height:40px;background-color:#2563eb;border-radius:12px;color:#ffffff;font-size:18px;font-weight:700;">L</td>
                      <td style="padding-left:12px;">
                        <div style="font-size:14px;font-weight:600;line-height:18px;color:#1c1917;">LoanHub</div>
                        <div style="font-size:10px;font-weight:600;letter-spacing:2px;text-transform:uppercase;color:#a8a29e;">Finance Data</div>
                      </td>
                    </tr>
                  </table>
                </td></tr>
                <tr><td style="padding:24px;">
                  <p style="margin:0 0 6px;font-size:11px;font-weight:600;letter-spacing:1.6px;text-transform:uppercase;color:#a8a29e;">{{EYEBROW}}</p>
                  <h1 style="margin:0;font-size:20px;line-height:28px;font-weight:600;color:#1c1917;">{{TITLE}}</h1>
                  <p style="margin:8px 0 16px;font-size:14px;line-height:22px;color:#78716c;">{{DESCRIPTION}}</p>
                  <span style="display:inline-block;padding:4px 10px;border:1px solid transparent;border-radius:9999px;font-size:11px;font-weight:600;letter-spacing:.4px;{{CHIP_COLORS}}">{{STATUS}}</span>
                  <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" style="margin-top:20px;border:1px solid #e7e5e4;border-radius:8px;">
                    {{DETAILS}}
                  </table>
                  <p style="margin:24px 0 0;">
                    <a href="{{CTA_URL}}" style="display:inline-block;padding:10px 16px;background-color:#2563eb;border-radius:8px;color:#ffffff;text-decoration:none;font-size:14px;font-weight:600;">{{CTA_LABEL}}</a>
                  </p>
                </td></tr>
                <tr><td style="padding:14px 24px;border-top:1px solid #e7e5e4;font-size:12px;line-height:18px;color:#a8a29e;">
                  Email ini dikirim otomatis oleh LoanHub. Mohon jangan membalas email ini.
                </td></tr>
              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """;
    return template
        .replace("{{EYEBROW}}", escape(eyebrow))
        .replace("{{TITLE}}", escape(title))
        .replace("{{DESCRIPTION}}", escape(description))
        .replace("{{CHIP_COLORS}}", chipColors(status))
        .replace("{{STATUS}}", escape(status))
        .replace("{{DETAILS}}", renderDetails(details))
        .replace("{{CTA_URL}}", escape(ctaUrl))
        .replace("{{CTA_LABEL}}", escape(ctaLabel));
  }

  private String renderDetails(List<Detail> details) {
    StringBuilder rows = new StringBuilder();
    for (int i = 0; i < details.size(); i++) {
      Detail item = details.get(i);
      String border = i == details.size() - 1 ? "" : "border-bottom:1px solid #f5f5f4;";
      rows.append("<tr>")
          .append("<td style=\"width:38%;padding:11px 14px;font-size:11px;font-weight:600;letter-spacing:.6px;text-transform:uppercase;color:#78716c;")
          .append(border)
          .append("\">")
          .append(escape(item.label()))
          .append("</td>")
          .append("<td style=\"padding:11px 14px;font-size:14px;line-height:20px;font-weight:600;color:#1c1917;")
          .append(border)
          .append("\">")
          .append(escape(valueOrDash(item.value())))
          .append("</td>")
          .append("</tr>");
    }
    return rows.toString();
  }

  private String chipColors(String status) {
    if (status == null) return "background-color:#f5f5f4;color:#57534e;border-color:#e7e5e4;";
    return switch (status) {
      case "PENDING" -> "background-color:#fffbeb;color:#b45309;border-color:#fde68a;";
      case "APPROVED" -> "background-color:#ecfdf5;color:#047857;border-color:#a7f3d0;";
      case "REJECTED" -> "background-color:#fef2f2;color:#b91c1c;border-color:#fecaca;";
      case "CANCELLED" -> "background-color:#f5f5f4;color:#57534e;border-color:#e7e5e4;";
      default -> "background-color:#f5f5f4;color:#57534e;border-color:#e7e5e4;";
    };
  }

  private Detail detail(String label, String value) {
    return new Detail(label, value);
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
