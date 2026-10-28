package com.loan.loan_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loan.loan_system.entity.Loan;

@Repository 
public interface LoanRepository extends JpaRepository<Loan, UUID> {
  List<Loan> findByCustomerId(UUID customerId);
}
