package com.loan.loan_system.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan.loan_system.dto.request.CustomerRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.CustomerResponse;
import com.loan.loan_system.service.CustomerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor 
public class CustomerController {
  private final CustomerService customerService;

  @PostMapping ("/check-email")
  public ResponseEntity<ApiResponse<CustomerResponse>> checkEmailExists(@RequestBody CustomerRequest customerRequest) {
    boolean emailExists = customerService.isEmailExists(customerRequest.getEmail());
    String message = emailExists ? "Email sudah terdaftar" : "Email belum terdaftar";
    return ResponseEntity.ok(ApiResponse.success(message, null));
  }

  @PostMapping ("/check-identity-number")
  public ResponseEntity<ApiResponse<CustomerResponse>> checkIdentityNumberExists(
      @RequestBody CustomerRequest customerRequest) {
    boolean identityNumberExists = customerService.isIdentityNumberExists(customerRequest.getIdentityNumber());
    String message = identityNumberExists ? "Nomor identitas sudah terdaftar" : "Nomor identitas belum terdaftar";
    return ResponseEntity
        .ok(ApiResponse.success(message, null));
  }
  
  @PostMapping 
  public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@RequestBody CustomerRequest customerRequest) {
    CustomerResponse customerResponse = customerService.createCustomer(customerRequest);
    return ResponseEntity.ok(ApiResponse.success("Customer created successfully", customerResponse));
  }
  
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(@PathVariable UUID id, @RequestBody CustomerRequest customerRequest) {
    CustomerResponse customerResponse = customerService.updateCustomer(customerRequest, id);
    return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", customerResponse));
  }
}
