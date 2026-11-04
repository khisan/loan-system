package com.loan.loan_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loan.loan_system.entity.Collateral;

@Repository
public interface CollateralRepository extends JpaRepository<Collateral, UUID> {
  List<Collateral> findByLoanId(UUID loanId);
}
