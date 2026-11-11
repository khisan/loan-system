package com.loan.loan_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.loan.loan_system.entity.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
  List<Invoice> findByLoanId(UUID loanId);

  @Query(value = """
      SELECT EXISTS (
        SELECT 1 FROM invoices
        WHERE invoice_number = :invoiceNumber
          AND (:currentId IS NULL OR id <> :currentId)
      )
      """, nativeQuery = true)
  boolean existsByInvoiceNumber(
      @Param("invoiceNumber") String invoiceNumber,
      @Param("currentId") UUID currentId);
}
