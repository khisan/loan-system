package com.loan.loan_system.repository;

import com.loan.loan_system.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository 
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
  
  Optional<Customer> findByIdentityNumber(String identityNumber);

  boolean existsByEmail(String email);
  boolean existsByIdentityNumber(String identityNumber);
}
