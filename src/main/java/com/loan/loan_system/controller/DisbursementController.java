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
import com.loan.loan_system.dto.request.DisbursementRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.ApprovalRequestResponse;
import com.loan.loan_system.dto.response.DisbursementResponse;
import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.service.ApprovalService;
import com.loan.loan_system.service.DisbursementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/disbursements")
@RequiredArgsConstructor
public class DisbursementController {
  private final DisbursementService disbursementService;
  private final ApprovalService approvalService;
  private final ObjectMapper objectMapper;

  @GetMapping
  public ResponseEntity<ApiResponse<List<DisbursementResponse>>> getDisbursements() {
    return ResponseEntity.ok(ApiResponse.success(
        "Disbursements retrieved successfully", disbursementService.getDisbursements()));
  }

  @GetMapping("/loan/{loanId}")
  public ResponseEntity<ApiResponse<List<DisbursementResponse>>> getDisbursementsByLoanId(
      @PathVariable UUID loanId) {
    return ResponseEntity.ok(ApiResponse.success(
        "Loan disbursements retrieved successfully",
        disbursementService.getDisbursementsByLoanId(loanId)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<DisbursementResponse>> getDisbursementById(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(
        "Disbursement retrieved successfully", disbursementService.getDisbursementById(id)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> createDisbursement(
      @Valid @RequestBody DisbursementRequest request) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.DISBURSEMENT, ApprovalOperation.CREATE, null,
        payload(request), null, "Pengajuan pencairan baru")));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> updateDisbursement(
      @PathVariable UUID id, @Valid @RequestBody DisbursementRequest request) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.DISBURSEMENT, ApprovalOperation.UPDATE, id,
        payload(request), null, "Pengajuan perubahan pencairan")));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> deleteDisbursement(@PathVariable UUID id) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.DISBURSEMENT, ApprovalOperation.DELETE, id,
        Map.of(), null, "Pengajuan hapus pencairan")));
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
