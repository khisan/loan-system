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
import com.loan.loan_system.dto.request.GuarantorRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.ApprovalRequestResponse;
import com.loan.loan_system.dto.response.GuarantorResponse;
import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.service.ApprovalService;
import com.loan.loan_system.service.GuarantorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/guarantors")
@RequiredArgsConstructor
public class GuarantorController {
  private final GuarantorService guarantorService;
  private final ApprovalService approvalService;
  private final ObjectMapper objectMapper;

  @GetMapping
  public ResponseEntity<ApiResponse<List<GuarantorResponse>>> getGuarantors() {
    return ResponseEntity.ok(ApiResponse.success(
        "Guarantors retrieved successfully", guarantorService.getGuarantors()));
  }

  @GetMapping("/loan/{loanId}")
  public ResponseEntity<ApiResponse<List<GuarantorResponse>>> getGuarantorsByLoanId(
      @PathVariable UUID loanId) {
    return ResponseEntity.ok(ApiResponse.success(
        "Loan guarantors retrieved successfully",
        guarantorService.getGuarantorsByLoanId(loanId)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<GuarantorResponse>> getGuarantorById(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(
        "Guarantor retrieved successfully", guarantorService.getGuarantorById(id)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> createGuarantor(
      @Valid @RequestBody GuarantorRequest request) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.GUARANTOR, ApprovalOperation.CREATE, null,
        payload(request), null, "Pengajuan penjamin baru")));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> updateGuarantor(
      @PathVariable UUID id, @Valid @RequestBody GuarantorRequest request) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.GUARANTOR, ApprovalOperation.UPDATE, id,
        payload(request), null, "Pengajuan perubahan penjamin")));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> deleteGuarantor(@PathVariable UUID id) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.GUARANTOR, ApprovalOperation.DELETE, id,
        Map.of(), null, "Pengajuan hapus penjamin")));
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
