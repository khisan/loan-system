package com.loan.loan_system.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api")
@RequiredArgsConstructor 
public class CustomerController {
  private final CustomerService customerService;

  @GetMapping("/customers")
  public ResponseEntity<ApiResponse<List<CustomerResponse>>> getCustomers() {
    List<CustomerResponse> customerResponses = customerService.getCustomers();
    return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", customerResponses));
  }
  
  @GetMapping ("/customer/{id}")
  public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable UUID id) {
    CustomerResponse customerResponse = customerService.getCustomerById(id);
    return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", customerResponse));
  }

  @PostMapping ("/customer")
  public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@RequestBody CustomerRequest customerRequest) {
    boolean emailExists = customerService.isEmailExists(customerRequest.getEmail());
    boolean identityNumberExists = customerService.isIdentityNumberExists(customerRequest.getIdentityNumber());
    if (emailExists) {
      return ResponseEntity.status(400).body(ApiResponse.failure("Email sudah terdaftar"));
    }
    if (identityNumberExists) {
      return ResponseEntity.status(400).body(ApiResponse.failure("Nomor identitas sudah terdaftar"));
    }
    CustomerResponse customerResponse = customerService.createCustomer(customerRequest);
    return ResponseEntity.ok(ApiResponse.success("Customer created successfully", customerResponse));
  }
  
  @PutMapping("/customer/{id}")
  public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(@PathVariable UUID id,
      @RequestBody CustomerRequest customerRequest) {
    CustomerResponse customerResponse = customerService.updateCustomer(customerRequest, id);
    return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", customerResponse));
  }
  

}
