package com.loan.loan_system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.loan.loan_system.dto.request.InvoiceRequest;
import com.loan.loan_system.dto.response.InvoiceResponse;
import com.loan.loan_system.entity.Invoice;
import com.loan.loan_system.entity.Loan;
import com.loan.loan_system.repository.InvoiceRepository;
import com.loan.loan_system.repository.LoanRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceService {
  private final InvoiceRepository invoiceRepository;
  private final LoanRepository loanRepository;

  @Transactional(readOnly = true)
  public List<InvoiceResponse> getInvoices() {
    return invoiceRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<InvoiceResponse> getInvoicesByLoanId(UUID loanId) {
    return invoiceRepository.findByLoanId(loanId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public InvoiceResponse getInvoiceById(UUID id) {
    return toResponse(findInvoiceById(id));
  }

  @Transactional
  public InvoiceResponse createInvoice(InvoiceRequest request) {
    ensureInvoiceNumberAvailable(request.getInvoiceNumber(), null);
    Invoice invoice = Invoice.builder()
        .loan(findLoanById(request.getLoanId()))
        .invoiceNumber(request.getInvoiceNumber())
        .installmentNumber(request.getInstallmentNumber())
        .principalAmount(request.getPrincipalAmount())
        .interestAmount(request.getInterestAmount())
        .penaltyAmount(normalizePenalty(request.getPenaltyAmount()))
        .totalAmount(calculateTotal(
            request.getPrincipalAmount(), request.getInterestAmount(), request.getPenaltyAmount()))
        .dueDate(request.getDueDate())
        .paidAt(request.getPaidAt())
        .status(request.getStatus())
        .build();
    return toResponse(invoiceRepository.save(invoice));
  }

  @Transactional
  public InvoiceResponse updateInvoice(UUID id, InvoiceRequest request) {
    Invoice invoice = findInvoiceById(id);
    ensureInvoiceNumberAvailable(request.getInvoiceNumber(), id);
    invoice.setLoan(findLoanById(request.getLoanId()));
    invoice.setInvoiceNumber(request.getInvoiceNumber());
    invoice.setInstallmentNumber(request.getInstallmentNumber());
    invoice.setPrincipalAmount(request.getPrincipalAmount());
    invoice.setInterestAmount(request.getInterestAmount());
    invoice.setPenaltyAmount(normalizePenalty(request.getPenaltyAmount()));
    invoice.setTotalAmount(
        calculateTotal(request.getPrincipalAmount(), request.getInterestAmount(), request.getPenaltyAmount()));
    invoice.setDueDate(request.getDueDate());
    invoice.setPaidAt(request.getPaidAt());
    if (request.getStatus() != null) {
      invoice.setStatus(request.getStatus());
    }
    return toResponse(invoiceRepository.save(invoice));
  }

  @Transactional
  public void deleteInvoice(UUID id) {
    invoiceRepository.delete(findInvoiceById(id));
  }

  private Invoice findInvoiceById(UUID id) {
    return invoiceRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Invoice not found with ID: " + id));
  }

  private Loan findLoanById(UUID id) {
    return loanRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Loan not found with ID: " + id));
  }

  private void ensureInvoiceNumberAvailable(String invoiceNumber, UUID currentId) {
    if (invoiceRepository.existsByInvoiceNumber(invoiceNumber, currentId)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Invoice number already exists: " + invoiceNumber);
    }
  }

  private BigDecimal normalizePenalty(BigDecimal penaltyAmount) {
    return penaltyAmount == null ? BigDecimal.ZERO : penaltyAmount;
  }

  private BigDecimal calculateTotal(
      BigDecimal principalAmount, BigDecimal interestAmount, BigDecimal penaltyAmount) {
    return principalAmount.add(interestAmount).add(normalizePenalty(penaltyAmount));
  }

  private InvoiceResponse toResponse(Invoice invoice) {
    return InvoiceResponse.builder()
        .id(invoice.getId())
        .loanId(invoice.getLoan().getId())
        .invoiceNumber(invoice.getInvoiceNumber())
        .installmentNumber(invoice.getInstallmentNumber())
        .principalAmount(invoice.getPrincipalAmount())
        .interestAmount(invoice.getInterestAmount())
        .penaltyAmount(invoice.getPenaltyAmount())
        .totalAmount(invoice.getTotalAmount())
        .dueDate(invoice.getDueDate())
        .paidAt(invoice.getPaidAt())
        .status(invoice.getStatus())
        .createdAt(invoice.getCreatedAt())
        .updatedAt(invoice.getUpdatedAt())
        .build();
  }
}
