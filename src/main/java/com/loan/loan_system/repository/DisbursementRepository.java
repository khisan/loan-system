package com.loan.loan_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loan.loan_system.entity.Disbursement;

@Repository
public interface DisbursementRepository extends JpaRepository<Disbursement, UUID> {
  List<Disbursement> findByLoanId(UUID loanId);

  boolean existsByDisbursementNumber(String disbursementNumber);

  boolean existsByDisbursementNumberAndIdNot(String disbursementNumber, UUID id);
}
