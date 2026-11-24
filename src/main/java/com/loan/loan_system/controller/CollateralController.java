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
import com.loan.loan_system.dto.request.CollateralRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.ApprovalRequestResponse;
import com.loan.loan_system.dto.response.CollateralResponse;
import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.service.ApprovalService;
import com.loan.loan_system.service.CollateralService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/collaterals")
@RequiredArgsConstructor
public class CollateralController {
  private final CollateralService collateralService;
  private final ApprovalService approvalService;
  private final ObjectMapper objectMapper;

  @GetMapping
  public ResponseEntity<ApiResponse<List<CollateralResponse>>> getCollaterals() {
    return ResponseEntity.ok(ApiResponse.success(
        "Collaterals retrieved successfully", collateralService.getCollaterals()));
  }

  @GetMapping("/loan/{loanId}")
  public ResponseEntity<ApiResponse<List<CollateralResponse>>> getCollateralsByLoanId(
      @PathVariable UUID loanId) {
    return ResponseEntity.ok(ApiResponse.success(
        "Loan collaterals retrieved successfully",
        collateralService.getCollateralsByLoanId(loanId)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CollateralResponse>> getCollateralById(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(
        "Collateral retrieved successfully", collateralService.getCollateralById(id)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> createCollateral(
      @Valid @RequestBody CollateralRequest request) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.COLLATERAL, ApprovalOperation.CREATE, null,
        payload(request), null, "Pengajuan agunan baru")));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> updateCollateral(
      @PathVariable UUID id, @Valid @RequestBody CollateralRequest request) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.COLLATERAL, ApprovalOperation.UPDATE, id,
        payload(request), null, "Pengajuan perubahan agunan")));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> deleteCollateral(@PathVariable UUID id) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.COLLATERAL, ApprovalOperation.DELETE, id,
        Map.of(), null, "Pengajuan hapus agunan")));
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
