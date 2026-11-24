package com.loan.loan_system.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan.loan_system.dto.request.ChangeRequest;
import com.loan.loan_system.dto.request.InvoiceRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.ApprovalRequestResponse;
import com.loan.loan_system.dto.response.InvoiceResponse;
import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.service.ApprovalService;
import com.loan.loan_system.service.InvoiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {
  private final InvoiceService invoiceService;
  private final ApprovalService approvalService;
  private final ObjectMapper objectMapper;

  @GetMapping
  public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoices() {
    return ResponseEntity.ok(ApiResponse.success(
        "Invoices retrieved successfully", invoiceService.getInvoices()));
  }

  @GetMapping("/loan/{loanId}")
  public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByLoanId(
      @PathVariable UUID loanId) {
    return ResponseEntity.ok(ApiResponse.success(
        "Loan invoices retrieved successfully", invoiceService.getInvoicesByLoanId(loanId)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(
        "Invoice retrieved successfully", invoiceService.getInvoiceById(id)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> createInvoice(
      @Valid @RequestBody InvoiceRequest request) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.INVOICE, ApprovalOperation.CREATE, null,
        payload(request), null, "Pengajuan faktur baru")));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> updateInvoice(
      @PathVariable UUID id, @Valid @RequestBody InvoiceRequest request) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.INVOICE, ApprovalOperation.UPDATE, id,
        payload(request), null, "Pengajuan perubahan faktur")));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> deleteInvoice(@PathVariable UUID id) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.INVOICE, ApprovalOperation.DELETE, id,
        Map.of(), null, "Pengajuan hapus faktur")));
  }

  private ResponseEntity<ApiResponse<ApprovalRequestResponse>> proposalResponse(
      ApprovalRequestResponse proposal) {
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(
        "Proposal dikirim. Menunggu persetujuan finance/admin.", proposal));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> payload(Object request) {
    return objectMapper.convertValue(request, Map.class);
  }
}
