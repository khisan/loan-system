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

import com.loan.loan_system.dto.request.CollateralRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.CollateralResponse;
import com.loan.loan_system.service.CollateralService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/collaterals")
@RequiredArgsConstructor
public class CollateralController {
  private final CollateralService collateralService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<CollateralResponse>>> getCollaterals() {
    return ResponseEntity.ok(
        ApiResponse.success("Collaterals retrieved successfully", collateralService.getCollaterals()));
  }

  @GetMapping("/loan/{loanId}")
  public ResponseEntity<ApiResponse<List<CollateralResponse>>> getCollateralsByLoanId(@PathVariable UUID loanId) {
    return ResponseEntity.ok(ApiResponse.success(
        "Loan collaterals retrieved successfully", collateralService.getCollateralsByLoanId(loanId)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CollateralResponse>> getCollateralById(@PathVariable UUID id) {
    return ResponseEntity.ok(
        ApiResponse.success("Collateral retrieved successfully", collateralService.getCollateralById(id)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<CollateralResponse>> createCollateral(
      @Valid @RequestBody CollateralRequest request) {
    CollateralResponse collateral = collateralService.createCollateral(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Collateral created successfully", collateral));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<CollateralResponse>> updateCollateral(
      @PathVariable UUID id,
      @Valid @RequestBody CollateralRequest request) {
    return ResponseEntity.ok(ApiResponse.success(
        "Collateral updated successfully", collateralService.updateCollateral(id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteCollateral(@PathVariable UUID id) {
    collateralService.deleteCollateral(id);
    return ResponseEntity.ok(ApiResponse.success("Collateral deleted successfully", null));
  }
}
