package com.loan.loan_system.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loan.loan_system.entity.Customer;
import com.loan.loan_system.repository.CustomerRepository;
import com.loan.loan_system.dto.response.CustomerResponse;
import com.loan.loan_system.dto.request.CustomerRequest;
import com.loan.loan_system.security.Actor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {
  private final CustomerRepository customerRepository;

  public boolean isIdExists(UUID id) {
    return customerRepository.existsById(id);
  }

  public boolean isEmailExists(String email) {
    return customerRepository.existsByEmail(email);
  }

  public boolean isIdentityNumberExists(String identityNumber) {
    return customerRepository.existsByIdentityNumber(identityNumber);
  }

  /** Applies an approved CREATE proposal. Numbers are reserved by ApprovalService. */
  @Transactional
  public CustomerResponse applyCreate(CustomerRequest request, String businessNumber, Actor actor) {
    Customer customer = Customer.builder()
        .customerNumber(businessNumber)
        .fullName(request.getFullName())
        .identityNumber(request.getIdentityNumber())
        .email(request.getEmail())
        .phoneNumber(request.getPhoneNumber())
        .address(request.getAddress())
        .createdById(actor.userId())
        .updatedById(actor.userId())
        .build();
    Customer saved = customerRepository.save(customer);
    return CustomerResponse.builder()
        .id(saved.getId())
        .customerNumber(saved.getCustomerNumber())
        .fullName(saved.getFullName())
        .identityNumber(saved.getIdentityNumber())
        .email(saved.getEmail())
        .phoneNumber(saved.getPhoneNumber())
        .address(saved.getAddress())
        .createdAt(saved.getCreatedAt())
        .build();
  }

  @Transactional
  public CustomerResponse applyUpdate(UUID id, CustomerRequest request, Actor actor) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
    customer.setFullName(request.getFullName());
    customer.setEmail(request.getEmail());
    customer.setPhoneNumber(request.getPhoneNumber());
    customer.setAddress(request.getAddress());
    customer.setUpdatedById(actor.userId());
    Customer updated = customerRepository.save(customer);
    return CustomerResponse.builder()
        .id(updated.getId())
        .customerNumber(updated.getCustomerNumber())
        .fullName(updated.getFullName())
        .identityNumber(updated.getIdentityNumber())
        .email(updated.getEmail())
        .phoneNumber(updated.getPhoneNumber())
        .address(updated.getAddress())
        .createdAt(updated.getCreatedAt())
        .updatedAt(updated.getUpdatedAt())
        .build();
  }

  @Transactional
  public void applyDelete(UUID id, Actor actor) {
    customerRepository.deleteById(id);
  }

  public List<CustomerResponse> getCustomers() {
    return customerRepository.findAll().stream()
        .map(customer -> CustomerResponse.builder()
            .id(customer.getId())
            .customerNumber(customer.getCustomerNumber())
            .fullName(customer.getFullName())
            .identityNumber(customer.getIdentityNumber())
            .email(customer.getEmail())
            .phoneNumber(customer.getPhoneNumber())
            .address(customer.getAddress())
            .createdAt(customer.getCreatedAt())
            .build())
        .collect(Collectors.toList());
  }

  public CustomerResponse getCustomerById(UUID id) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
    return CustomerResponse.builder()
        .id(customer.getId())
        .customerNumber(customer.getCustomerNumber())
        .fullName(customer.getFullName())
        .identityNumber(customer.getIdentityNumber())
        .email(customer.getEmail())
        .phoneNumber(customer.getPhoneNumber())
        .address(customer.getAddress())
        .createdAt(customer.getCreatedAt())
        .updatedAt(customer.getUpdatedAt())
        .build();
  }
}
