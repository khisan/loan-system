package com.loan.loan_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loan.loan_system.entity.Guarantor;

@Repository
public interface GuarantorRepository extends JpaRepository<Guarantor, UUID> {
  List<Guarantor> findByLoanId(UUID loanId);
}
