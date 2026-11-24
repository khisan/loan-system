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
import com.loan.loan_system.dto.request.CustomerRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.ApprovalRequestResponse;
import com.loan.loan_system.dto.response.CustomerResponse;
import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.service.ApprovalService;
import com.loan.loan_system.service.CustomerService;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CustomerController {
  private final CustomerService customerService;
  private final ApprovalService approvalService;
  private final ObjectMapper objectMapper;

  @GetMapping("/customers")
  public ResponseEntity<ApiResponse<List<CustomerResponse>>> getCustomers() {
    return ResponseEntity.ok(ApiResponse.success(
        "Customers retrieved successfully", customerService.getCustomers()));
  }

  @GetMapping("/customers/{id}")
  public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(
        "Customer retrieved successfully", customerService.getCustomerById(id)));
  }

  @PostMapping("/customers")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> createCustomer(
      @RequestBody CustomerRequest customerRequest) {
    if (customerService.isEmailExists(customerRequest.getEmail())) {
      return ResponseEntity.badRequest().body(ApiResponse.failure("Email sudah terdaftar"));
    }
    if (customerService.isIdentityNumberExists(customerRequest.getIdentityNumber())) {
      return ResponseEntity.badRequest().body(ApiResponse.failure("Nomor identitas sudah terdaftar"));
    }
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.CUSTOMER, ApprovalOperation.CREATE, null,
        payload(customerRequest), null, "Pengajuan nasabah baru")));
  }

  @PutMapping("/customers/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> updateCustomer(
      @PathVariable UUID id, @RequestBody CustomerRequest customerRequest) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.CUSTOMER, ApprovalOperation.UPDATE, id,
        payload(customerRequest), null, "Pengajuan perubahan data nasabah")));
  }

  @DeleteMapping("/customers/{id}")
  public ResponseEntity<ApiResponse<ApprovalRequestResponse>> deleteCustomer(@PathVariable UUID id) {
    return proposalResponse(approvalService.submit(new ChangeRequest(
        BusinessNumberType.CUSTOMER, ApprovalOperation.DELETE, id,
        Map.of(), null, "Pengajuan hapus nasabah")));
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
