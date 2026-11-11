package com.loan.loan_system.controller;

import java.util.List;
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

import com.loan.loan_system.dto.request.InvoiceRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.InvoiceResponse;
import com.loan.loan_system.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {
  private final InvoiceService invoiceService;

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
  public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
      @Valid @RequestBody InvoiceRequest request) {
    InvoiceResponse invoice = invoiceService.createInvoice(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Invoice created successfully", invoice));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoice(
      @PathVariable UUID id,
      @Valid @RequestBody InvoiceRequest request) {
    return ResponseEntity.ok(ApiResponse.success(
        "Invoice updated successfully", invoiceService.updateInvoice(id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable UUID id) {
    invoiceService.deleteInvoice(id);
    return ResponseEntity.ok(ApiResponse.success("Invoice deleted successfully", null));
  }
}
