package com.loan.loan_system.service;

import org.springframework.stereotype.Service;

import com.loan.loan_system.entity.Customer;
import com.loan.loan_system.repository.CustomerRepository;
import com.loan.loan_system.dto.response.CustomerResponse;
import com.loan.loan_system.dto.request.CustomerRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class CustomerService {
  private final CustomerRepository customerRepository;

  public boolean isEmailExists(String email) {
    return customerRepository.existsByEmail(email);
  }

  public boolean isIdentityNumberExists(String identityNumber) {
    return customerRepository.existsByIdentityNumber(identityNumber);
  }

  public CustomerResponse createCustomer(CustomerRequest customerRequest) {
    Customer customer = Customer.builder()
        .fullName(customerRequest.getFullName())
        .identityNumber(customerRequest.getIdentityNumber())
        .email(customerRequest.getEmail())
        .phoneNumber(customerRequest.getPhoneNumber())
        .address(customerRequest.getAddress())
        .build();
    Customer savedCustomer = customerRepository.save(customer);
    return CustomerResponse.builder()
        .id(savedCustomer.getId())
        .fullName(savedCustomer.getFullName())
        .identityNumber(savedCustomer.getIdentityNumber())
        .email(savedCustomer.getEmail())
        .phoneNumber(savedCustomer.getPhoneNumber())
        .address(savedCustomer.getAddress())
        .createdAt(savedCustomer.getCreatedAt())
        .build();
  }
}
