package com.loan.loan_system.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan.loan_system.dto.request.ChangeRequest;
import com.loan.loan_system.dto.request.LoanRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.ApprovalRequestResponse;
import com.loan.loan_system.dto.response.LoanResponse;
import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.enums.LoanStatus;
import com.loan.loan_system.service.ApprovalService;
import com.loan.loan_system.service.LoanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
  private final LoanService loanService;
  private final ApprovalService approvalService;
  private final ObjectMapper objectMapper;

  @GetMapping
  public ResponseEntity<ApiResponse<List<LoanResponse>>> getLoans() {
    return ResponseEntity.ok(ApiResponse.success(
        "Loans retrieved successfully", loanService.getLoans()));
  }

  @GetMapping("/customer/{customerId}")
  public ResponseEntity<ApiResponse<List<LoanResponse>>> getLoansByCustomerId(
      @PathVariable UUID customerId) {
    return ResponseEntity.ok(ApiResponse.success(
        "Customer loans retrieved successfully", loanService.getLoansByCustomerId(customerId)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<LoanResponse>> getLoanById(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(
        "Loan retrieved successfully", loanService.getLoanById(id)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> createLoan(
      @Valid @RequestBody LoanRequest request) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.LOAN, ApprovalOperation.CREATE, null,
        payload(request), null, "Pengajuan pinjaman baru")));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> updateLoan(
      @PathVariable UUID id, @Valid @RequestBody LoanRequest request) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.LOAN, ApprovalOperation.UPDATE, id,
        payload(request), null, "Pengajuan perubahan pinjaman")));
  }

  @PatchMapping("/{id}/approve")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> approveLoan(@PathVariable UUID id) {
    return statusProposal(id, LoanStatus.APPROVED, "Pengajuan persetujuan pinjaman");
  }

  @PatchMapping("/{id}/reject")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> rejectLoan(@PathVariable UUID id) {
    return statusProposal(id, LoanStatus.REJECTED, "Pengajuan penolakan pinjaman");
  }

  @PatchMapping("/{id}/disburse")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> disburseLoan(@PathVariable UUID id) {
    return statusProposal(id, LoanStatus.DISBURSED, "Pengajuan pencairan pinjaman");
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> deleteLoan(@PathVariable UUID id) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.LOAN, ApprovalOperation.DELETE, id,
        Map.of(), null, "Pengajuan hapus pinjaman")));
  }

  private ResponseEntity<ApiResponse<ApprovalRequestResponse>> statusProposal(
      UUID id, LoanStatus target, String reason) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.LOAN, ApprovalOperation.STATUS_CHANGE, id,
        Map.of(), target.name(), reason)));
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
