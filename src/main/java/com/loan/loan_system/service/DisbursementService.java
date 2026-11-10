package com.loan.loan_system.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.loan.loan_system.dto.request.DisbursementRequest;
import com.loan.loan_system.dto.response.DisbursementResponse;
import com.loan.loan_system.entity.Disbursement;
import com.loan.loan_system.entity.Loan;
import com.loan.loan_system.repository.DisbursementRepository;
import com.loan.loan_system.repository.LoanRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DisbursementService {
  private final DisbursementRepository disbursementRepository;
  private final LoanRepository loanRepository;

  @Transactional(readOnly = true)
  public List<DisbursementResponse> getDisbursements() {
    return disbursementRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<DisbursementResponse> getDisbursementsByLoanId(UUID loanId) {
    return disbursementRepository.findByLoanId(loanId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public DisbursementResponse getDisbursementById(UUID id) {
    return toResponse(findDisbursementById(id));
  }

  @Transactional
  public DisbursementResponse createDisbursement(DisbursementRequest request) {
    ensureDisbursementNumberAvailable(request.getDisbursementNumber(), null);
    Disbursement disbursement = Disbursement.builder()
        .loan(findLoanById(request.getLoanId()))
        .disbursementNumber(request.getDisbursementNumber())
        .amount(request.getAmount())
        .bankName(request.getBankName())
        .bankAccountNumber(request.getBankAccountNumber())
        .bankAccountName(request.getBankAccountName())
        .build();
    return toResponse(disbursementRepository.save(disbursement));
  }

  @Transactional
  public DisbursementResponse updateDisbursement(UUID id, DisbursementRequest request) {
    Disbursement disbursement = findDisbursementById(id);
    ensureDisbursementNumberAvailable(request.getDisbursementNumber(), id);
    disbursement.setLoan(findLoanById(request.getLoanId()));
    disbursement.setDisbursementNumber(request.getDisbursementNumber());
    disbursement.setAmount(request.getAmount());
    disbursement.setBankName(request.getBankName());
    disbursement.setBankAccountNumber(request.getBankAccountNumber());
    disbursement.setBankAccountName(request.getBankAccountName());
    return toResponse(disbursementRepository.save(disbursement));
  }

  @Transactional
  public void deleteDisbursement(UUID id) {
    disbursementRepository.delete(findDisbursementById(id));
  }

  private Disbursement findDisbursementById(UUID id) {
    return disbursementRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Disbursement not found with ID: " + id));
  }

  private Loan findLoanById(UUID id) {
    return loanRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Loan not found with ID: " + id));
  }

  private void ensureDisbursementNumberAvailable(String disbursementNumber, UUID currentId) {
    boolean exists = currentId == null
        ? disbursementRepository.existsByDisbursementNumber(disbursementNumber)
        : disbursementRepository.existsByDisbursementNumberAndIdNot(disbursementNumber, currentId);
    if (exists) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Disbursement number already exists: " + disbursementNumber);
    }
  }

  private DisbursementResponse toResponse(Disbursement disbursement) {
    return DisbursementResponse.builder()
        .id(disbursement.getId())
        .loanId(disbursement.getLoan().getId())
        .disbursementNumber(disbursement.getDisbursementNumber())
        .amount(disbursement.getAmount())
        .bankName(disbursement.getBankName())
        .bankAccountNumber(disbursement.getBankAccountNumber())
        .bankAccountName(disbursement.getBankAccountName())
        .status(disbursement.getStatus())
        .disbursedAt(disbursement.getDisbursedAt())
        .createdAt(disbursement.getCreatedAt())
        .updatedAt(disbursement.getUpdatedAt())
        .build();
  }
}
