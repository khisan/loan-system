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

import com.loan.loan_system.dto.request.GuarantorRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.GuarantorResponse;
import com.loan.loan_system.service.GuarantorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/guarantors")
@RequiredArgsConstructor
public class GuarantorController {
  private final GuarantorService guarantorService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<GuarantorResponse>>> getGuarantors() {
    return ResponseEntity.ok(
        ApiResponse.success("Guarantors retrieved successfully", guarantorService.getGuarantors()));
  }

  @GetMapping("/loan/{loanId}")
  public ResponseEntity<ApiResponse<List<GuarantorResponse>>> getGuarantorsByLoanId(
      @PathVariable UUID loanId) {
    return ResponseEntity.ok(ApiResponse.success(
        "Loan guarantors retrieved successfully", guarantorService.getGuarantorsByLoanId(loanId)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<GuarantorResponse>> getGuarantorById(@PathVariable UUID id) {
    return ResponseEntity.ok(
        ApiResponse.success("Guarantor retrieved successfully", guarantorService.getGuarantorById(id)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<GuarantorResponse>> createGuarantor(
      @Valid @RequestBody GuarantorRequest request) {
    GuarantorResponse guarantor = guarantorService.createGuarantor(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Guarantor created successfully", guarantor));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<GuarantorResponse>> updateGuarantor(
      @PathVariable UUID id,
      @Valid @RequestBody GuarantorRequest request) {
    return ResponseEntity.ok(ApiResponse.success(
        "Guarantor updated successfully", guarantorService.updateGuarantor(id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteGuarantor(@PathVariable UUID id) {
    guarantorService.deleteGuarantor(id);
    return ResponseEntity.ok(ApiResponse.success("Guarantor deleted successfully", null));
  }
}
