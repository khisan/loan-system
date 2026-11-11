package com.loan.loan_system.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.loan.loan_system.dto.request.GuarantorRequest;
import com.loan.loan_system.dto.response.GuarantorResponse;
import com.loan.loan_system.entity.Guarantor;
import com.loan.loan_system.entity.Loan;
import com.loan.loan_system.repository.GuarantorRepository;
import com.loan.loan_system.repository.LoanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuarantorService {
  private final GuarantorRepository guarantorRepository;
  private final LoanRepository loanRepository;

  @Transactional(readOnly = true)
  public List<GuarantorResponse> getGuarantors() {
    return guarantorRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<GuarantorResponse> getGuarantorsByLoanId(UUID loanId) {
    return guarantorRepository.findByLoanId(loanId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public GuarantorResponse getGuarantorById(UUID id) {
    return toResponse(findGuarantorById(id));
  }

  @Transactional
  public GuarantorResponse createGuarantor(GuarantorRequest request) {
    Guarantor guarantor = Guarantor.builder()
        .loan(findLoanById(request.getLoanId()))
        .fullName(request.getFullName())
        .identityNumber(request.getIdentityNumber())
        .email(request.getEmail())
        .phoneNumber(request.getPhoneNumber())
        .relationship(request.getRelationship())
        .address(request.getAddress())
        .build();
    return toResponse(guarantorRepository.save(guarantor));
  }

  @Transactional
  public GuarantorResponse updateGuarantor(UUID id, GuarantorRequest request) {
    Guarantor guarantor = findGuarantorById(id);
    guarantor.setLoan(findLoanById(request.getLoanId()));
    guarantor.setFullName(request.getFullName());
    guarantor.setIdentityNumber(request.getIdentityNumber());
    guarantor.setEmail(request.getEmail());
    guarantor.setPhoneNumber(request.getPhoneNumber());
    guarantor.setRelationship(request.getRelationship());
    guarantor.setAddress(request.getAddress());
    return toResponse(guarantorRepository.save(guarantor));
  }

  @Transactional
  public void deleteGuarantor(UUID id) {
    guarantorRepository.delete(findGuarantorById(id));
  }

  private Guarantor findGuarantorById(UUID id) {
    return guarantorRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Guarantor not found with ID: " + id));
  }

  private Loan findLoanById(UUID id) {
    return loanRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Loan not found with ID: " + id));
  }

  private GuarantorResponse toResponse(Guarantor guarantor) {
    return GuarantorResponse.builder()
        .id(guarantor.getId())
        .loanId(guarantor.getLoan().getId())
        .fullName(guarantor.getFullName())
        .identityNumber(guarantor.getIdentityNumber())
        .email(guarantor.getEmail())
        .phoneNumber(guarantor.getPhoneNumber())
        .relationship(guarantor.getRelationship())
        .address(guarantor.getAddress())
        .createdAt(guarantor.getCreatedAt())
        .updatedAt(guarantor.getUpdatedAt())
        .build();
  }
}
