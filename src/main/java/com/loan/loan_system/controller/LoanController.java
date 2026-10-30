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

import com.loan.loan_system.dto.request.LoanRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.LoanResponse;
import com.loan.loan_system.service.LoanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
  private final LoanService loanService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<LoanResponse>>> getLoans() {
    return ResponseEntity.ok(ApiResponse.success("Loans retrieved successfully", loanService.getLoans()));
  }

  @GetMapping("/customer/{customerId}")
  public ResponseEntity<ApiResponse<List<LoanResponse>>> getLoansByCustomerId(@PathVariable UUID customerId) {
    return ResponseEntity.ok(
        ApiResponse.success("Customer loans retrieved successfully", loanService.getLoansByCustomerId(customerId)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<LoanResponse>> getLoanById(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success("Loan retrieved successfully", loanService.getLoanById(id)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<LoanResponse>> createLoan(@Valid @RequestBody LoanRequest request) {
    LoanResponse loan = loanService.createLoan(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Loan created successfully", loan));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<LoanResponse>> updateLoan(
      @PathVariable UUID id,
      @Valid @RequestBody LoanRequest request) {
    return ResponseEntity.ok(ApiResponse.success("Loan updated successfully", loanService.updateLoan(id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteLoan(@PathVariable UUID id) {
    loanService.deleteLoan(id);
    return ResponseEntity.ok(ApiResponse.success("Loan deleted successfully", null));
  }
}
