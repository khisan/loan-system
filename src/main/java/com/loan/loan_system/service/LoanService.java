package com.loan.loan_system.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.loan.loan_system.dto.request.LoanRequest;
import com.loan.loan_system.dto.response.LoanResponse;
import com.loan.loan_system.entity.Customer;
import com.loan.loan_system.entity.Loan;
import com.loan.loan_system.enums.LoanStatus;
import com.loan.loan_system.repository.CustomerRepository;
import com.loan.loan_system.repository.LoanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanService {
  private final LoanRepository loanRepository;
  private final CustomerRepository customerRepository;

  @Transactional(readOnly = true)
  public List<LoanResponse> getLoans() {
    return loanRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<LoanResponse> getLoansByCustomerId(UUID customerId) {
    return loanRepository.findByCustomerId(customerId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public LoanResponse getLoanById(UUID id) {
    return toResponse(findLoanById(id));
  }

  @Transactional
  public LoanResponse createLoan(LoanRequest request) {
    rejectStatusChange(request);
    Customer customer = findCustomerById(request.getCustomerId());
    Loan loan = Loan.builder()
        .customer(customer)
        .amount(request.getAmount())
        .interestRate(request.getInterestRate())
        .tenorMonths(request.getTenorMonths())
        .status(LoanStatus.PENDING)
        .build();
    return toResponse(loanRepository.save(loan));
  }

  @Transactional
  public LoanResponse updateLoan(UUID id, LoanRequest request) {
    rejectStatusChange(request);
    Loan loan = findLoanById(id);
    loan.setCustomer(findCustomerById(request.getCustomerId()));
    loan.setAmount(request.getAmount());
    loan.setInterestRate(request.getInterestRate());
    loan.setTenorMonths(request.getTenorMonths());
    return toResponse(loanRepository.save(loan));
  }

  @Transactional
  public LoanResponse approveLoan(UUID id) {
    return changeStatus(id, LoanStatus.PENDING, LoanStatus.APPROVED);
  }

  @Transactional
  public LoanResponse rejectLoan(UUID id) {
    return changeStatus(id, LoanStatus.PENDING, LoanStatus.REJECTED);
  }

  @Transactional
  public LoanResponse disburseLoan(UUID id) {
    return changeStatus(id, LoanStatus.APPROVED, LoanStatus.DISBURSED);
  }

  @Transactional
  public void deleteLoan(UUID id) {
    Loan loan = findLoanById(id);
    loanRepository.delete(loan);
  }

  private LoanResponse changeStatus(UUID id, LoanStatus expectedStatus, LoanStatus newStatus) {
    Loan loan = findLoanById(id);
    if (loan.getStatus() != expectedStatus) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Loan status must be " + expectedStatus + " to change it to " + newStatus);
    }

    loan.setStatus(newStatus);
    return toResponse(loanRepository.save(loan));
  }

  private void rejectStatusChange(LoanRequest request) {
    if (request.getStatus() != null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Loan status can only be changed through the role-specific status actions");
    }
  }

  private Loan findLoanById(UUID id) {
    return loanRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found with ID: " + id));
  }

  private Customer findCustomerById(UUID id) {
    return customerRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with ID: " + id));
  }

  private LoanResponse toResponse(Loan loan) {
    return LoanResponse.builder()
        .id(loan.getId())
        .customerId(loan.getCustomer().getId())
        .amount(loan.getAmount())
        .interestRate(loan.getInterestRate())
        .tenorMonths(loan.getTenorMonths())
        .status(loan.getStatus())
        .createdAt(loan.getCreatedAt())
        .updatedAt(loan.getUpdatedAt())
        .build();
  }
}
