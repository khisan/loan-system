package com.loan.loan_system.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.loan.loan_system.dto.request.CollateralRequest;
import com.loan.loan_system.dto.response.CollateralResponse;
import com.loan.loan_system.entity.Collateral;
import com.loan.loan_system.entity.Loan;
import com.loan.loan_system.repository.CollateralRepository;
import com.loan.loan_system.repository.LoanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CollateralService {
  private final CollateralRepository collateralRepository;
  private final LoanRepository loanRepository;

  @Transactional(readOnly = true)
  public List<CollateralResponse> getCollaterals() {
    return collateralRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<CollateralResponse> getCollateralsByLoanId(UUID loanId) {
    return collateralRepository.findByLoanId(loanId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public CollateralResponse getCollateralById(UUID id) {
    return toResponse(findCollateralById(id));
  }

  @Transactional
  public CollateralResponse createCollateral(CollateralRequest request) {
    Loan loan = findLoanById(request.getLoanId());
    Collateral collateral = Collateral.builder()
        .loan(loan)
        .type(request.getType())
        .documentNumber(request.getDocumentNumber())
        .marketValue(request.getMarketValue())
        .pledgedValue(request.getPledgedValue())
        .description(request.getDescription())
        .build();
    return toResponse(collateralRepository.save(collateral));
  }

  @Transactional
  public CollateralResponse updateCollateral(UUID id, CollateralRequest request) {
    Collateral collateral = findCollateralById(id);
    collateral.setLoan(findLoanById(request.getLoanId()));
    collateral.setType(request.getType());
    collateral.setDocumentNumber(request.getDocumentNumber());
    collateral.setMarketValue(request.getMarketValue());
    collateral.setPledgedValue(request.getPledgedValue());
    collateral.setDescription(request.getDescription());
    return toResponse(collateralRepository.save(collateral));
  }

  @Transactional
  public void deleteCollateral(UUID id) {
    collateralRepository.delete(findCollateralById(id));
  }

  private Collateral findCollateralById(UUID id) {
    return collateralRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Collateral not found with ID: " + id));
  }

  private Loan findLoanById(UUID id) {
    return loanRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Loan not found with ID: " + id));
  }

  private CollateralResponse toResponse(Collateral collateral) {
    return CollateralResponse.builder()
        .id(collateral.getId())
        .loanId(collateral.getLoan().getId())
        .type(collateral.getType())
        .documentNumber(collateral.getDocumentNumber())
        .marketValue(collateral.getMarketValue())
        .pledgedValue(collateral.getPledgedValue())
        .description(collateral.getDescription())
        .createdAt(collateral.getCreatedAt())
        .updatedAt(collateral.getUpdatedAt())
        .build();
  }
}
