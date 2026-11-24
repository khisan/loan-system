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
import com.loan.loan_system.security.Actor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceService {
  private final InvoiceRepository invoiceRepository;
  private final LoanRepository loanRepository;

  @Transactional(readOnly = true)
  public List<InvoiceResponse> getInvoices() {
    return invoiceRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<InvoiceResponse> getInvoicesByLoanId(UUID loanId) {
    return invoiceRepository.findByLoanId(loanId).stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public InvoiceResponse getInvoiceById(UUID id) {
    return toResponse(findInvoiceById(id));
  }

  @Transactional
  public InvoiceResponse applyCreate(InvoiceRequest request, String businessNumber, Actor actor) {
    Invoice invoice = Invoice.builder()
        .loan(findLoanById(request.getLoanId()))
        .invoiceNumber(businessNumber)
        .installmentNumber(request.getInstallmentNumber())
        .principalAmount(request.getPrincipalAmount())
        .interestAmount(request.getInterestAmount())
        .penaltyAmount(normalizePenalty(request.getPenaltyAmount()))
        .totalAmount(calculateTotal(
            request.getPrincipalAmount(), request.getInterestAmount(), request.getPenaltyAmount()))
        .dueDate(request.getDueDate())
        .paidAt(request.getPaidAt())
        .status(request.getStatus())
        .createdById(actor.userId())
        .updatedById(actor.userId())
        .build();
    return toResponse(invoiceRepository.save(invoice));
  }

  @Transactional
  public InvoiceResponse applyUpdate(UUID id, InvoiceRequest request, Actor actor) {
    Invoice invoice = findInvoiceById(id);
    invoice.setLoan(findLoanById(request.getLoanId()));
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
    invoice.setUpdatedById(actor.userId());
    return toResponse(invoiceRepository.save(invoice));
  }

  @Transactional
  public void applyDelete(UUID id, Actor actor) {
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
        .loanNumber(invoice.getLoan().getLoanNumber())
        .customerName(invoice.getLoan().getCustomer().getFullName())
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
