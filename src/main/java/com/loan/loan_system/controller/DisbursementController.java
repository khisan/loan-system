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

import com.loan.loan_system.dto.request.DisbursementRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.DisbursementResponse;
import com.loan.loan_system.service.DisbursementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/disbursements")
@RequiredArgsConstructor
public class DisbursementController {
  private final DisbursementService disbursementService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<DisbursementResponse>>> getDisbursements() {
    return ResponseEntity.ok(ApiResponse.success(
        "Disbursements retrieved successfully", disbursementService.getDisbursements()));
  }

  @GetMapping("/loan/{loanId}")
  public ResponseEntity<ApiResponse<List<DisbursementResponse>>> getDisbursementsByLoanId(
      @PathVariable UUID loanId) {
    return ResponseEntity.ok(ApiResponse.success(
        "Loan disbursements retrieved successfully", disbursementService.getDisbursementsByLoanId(loanId)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<DisbursementResponse>> getDisbursementById(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(
        "Disbursement retrieved successfully", disbursementService.getDisbursementById(id)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<DisbursementResponse>> createDisbursement(
      @Valid @RequestBody DisbursementRequest request) {
    DisbursementResponse disbursement = disbursementService.createDisbursement(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Disbursement created successfully", disbursement));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<DisbursementResponse>> updateDisbursement(
      @PathVariable UUID id,
      @Valid @RequestBody DisbursementRequest request) {
    return ResponseEntity.ok(ApiResponse.success(
        "Disbursement updated successfully", disbursementService.updateDisbursement(id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteDisbursement(@PathVariable UUID id) {
    disbursementService.deleteDisbursement(id);
    return ResponseEntity.ok(ApiResponse.success("Disbursement deleted successfully", null));
  }
}
