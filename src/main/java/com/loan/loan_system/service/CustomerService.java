package com.loan.loan_system.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

public boolean isIdExists(UUID id) {
    return customerRepository.existsById(id);
  }

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

  public CustomerResponse updateCustomer(CustomerRequest customerRequest, UUID id) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
    customer.setFullName(customerRequest.getFullName());
    customer.setEmail(customerRequest.getEmail());
    customer.setPhoneNumber(customerRequest.getPhoneNumber());
    customer.setAddress(customerRequest.getAddress());
    Customer updatedCustomer = customerRepository.save(customer);
    return CustomerResponse.builder()
        .id(updatedCustomer.getId())
        .fullName(updatedCustomer.getFullName())
        .identityNumber(updatedCustomer.getIdentityNumber())
        .email(updatedCustomer.getEmail())
        .phoneNumber(updatedCustomer.getPhoneNumber())
        .address(updatedCustomer.getAddress())
        .createdAt(updatedCustomer.getCreatedAt())
        .updatedAt(updatedCustomer.getUpdatedAt())
        .build();
  }

  public List<CustomerResponse> getCustomers() {
    List<Customer> customers = customerRepository.findAll();
    return customers.stream()
        .map(customer -> CustomerResponse.builder()
            .id(customer.getId())
            .fullName(customer.getFullName())
            .identityNumber(customer.getIdentityNumber())
            .email(customer.getEmail())
            .phoneNumber(customer.getPhoneNumber())
            .address(customer.getAddress())
            .createdAt(customer.getCreatedAt())
            .build())
        .collect(Collectors.toList());
  }

  public CustomerResponse getCustomerById(UUID id){
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
     return CustomerResponse.builder()
        .id(customer.getId())
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
