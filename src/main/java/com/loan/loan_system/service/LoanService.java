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
import com.loan.loan_system.security.Actor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanService {
  private final LoanRepository loanRepository;
  private final CustomerRepository customerRepository;

  @Transactional(readOnly = true)
  public List<LoanResponse> getLoans() {
    return loanRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<LoanResponse> getLoansByCustomerId(UUID customerId) {
    return loanRepository.findByCustomerId(customerId).stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public LoanResponse getLoanById(UUID id) {
    return toResponse(findLoanById(id));
  }

  @Transactional
  public LoanResponse applyCreate(LoanRequest request, String loanNumber, Actor actor) {
    rejectStatusChange(request);
    Loan loan = Loan.builder()
        .customer(findCustomerById(request.getCustomerId()))
        .loanNumber(loanNumber)
        .amount(request.getAmount())
        .interestRate(request.getInterestRate())
        .tenorMonths(request.getTenorMonths())
        .status(LoanStatus.PENDING)
        .createdById(actor.userId())
        .updatedById(actor.userId())
        .build();
    return toResponse(loanRepository.save(loan));
  }

  @Transactional
  public LoanResponse applyUpdate(UUID id, LoanRequest request, Actor actor) {
    rejectStatusChange(request);
    Loan loan = findLoanById(id);
    loan.setCustomer(findCustomerById(request.getCustomerId()));
    loan.setAmount(request.getAmount());
    loan.setInterestRate(request.getInterestRate());
    loan.setTenorMonths(request.getTenorMonths());
    loan.setUpdatedById(actor.userId());
    return toResponse(loanRepository.save(loan));
  }

  @Transactional
  public void applyDelete(UUID id, Actor actor) {
    loanRepository.delete(findLoanById(id));
  }

  /** Final status mutation, only called by the approval listener. */
  @Transactional
  public LoanResponse applyStatusChange(UUID id, LoanStatus newStatus, Actor actor) {
    Loan loan = findLoanById(id);
    LoanStatus expectedStatus = switch (newStatus) {
      case APPROVED, REJECTED -> LoanStatus.PENDING;
      case DISBURSED -> LoanStatus.APPROVED;
      default -> throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Status " + newStatus + " tidak bisa diubah lewat approval");
    };
    if (loan.getStatus() != expectedStatus) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Loan status must be " + expectedStatus + " to change it to " + newStatus);
    }
    loan.setStatus(newStatus);
    loan.setUpdatedById(actor.userId());
    if (newStatus == LoanStatus.APPROVED || newStatus == LoanStatus.DISBURSED) {
      loan.setApprovedById(actor.userId());
    }
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
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Loan not found with ID: " + id));
  }

  private Customer findCustomerById(UUID id) {
    return customerRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Customer not found with ID: " + id));
  }

  private LoanResponse toResponse(Loan loan) {
    return LoanResponse.builder()
        .id(loan.getId())
        .loanNumber(loan.getLoanNumber())
        .customerId(loan.getCustomer().getId())
        .customerName(loan.getCustomer().getFullName())
        .amount(loan.getAmount())
        .interestRate(loan.getInterestRate())
        .tenorMonths(loan.getTenorMonths())
        .status(loan.getStatus())
        .createdAt(loan.getCreatedAt())
        .updatedAt(loan.getUpdatedAt())
        .build();
  }
}
