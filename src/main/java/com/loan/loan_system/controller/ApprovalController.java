package com.loan.loan_system.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loan.loan_system.dto.request.DecisionRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.ApprovalRequestResponse;
import com.loan.loan_system.enums.ApprovalStatus;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.service.ApprovalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {
  private final ApprovalService approvalService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<ApprovalRequestResponse>>> list(
      @RequestParam(required = false) ApprovalStatus status,
      @RequestParam(required = false) BusinessNumberType entityType) {
    return ResponseEntity.ok(ApiResponse.success(
        "Approval requests retrieved successfully", approvalService.list(status, entityType)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> get(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(
        "Approval request retrieved successfully", approvalService.get(id)));
  }

  @PatchMapping("/{id}/approve")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> approve(
      @PathVariable UUID id, @RequestBody(required = false) DecisionRequest decision) {
    return ResponseEntity.ok(ApiResponse.success(
        "Proposal disetujui dan diterapkan",
        approvalService.approve(id, decision == null ? null : decision.getNote())));
  }

  @PatchMapping("/{id}/reject")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> reject(
      @PathVariable UUID id, @RequestBody DecisionRequest decision) {
    return ResponseEntity.ok(ApiResponse.success(
        "Proposal ditolak",
        approvalService.reject(id, decision == null ? null : decision.getNote())));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> cancel(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(
        "Proposal dibatalkan", approvalService.cancel(id)));
  }
}
