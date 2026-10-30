package com.loan.loan_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.loan.loan_system.enums.LoanStatus;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder 
public class Loan {
  
  @Id 
  @GeneratedValue (strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn (name = "customer_id", nullable = false)
  private Customer customer;

  @Column (nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column (name = "interest_rate", nullable = false)
  private Double interestRate;

  @Column (name = "tenor_months", nullable = false)
  private Integer tenorMonths;

  @Enumerated (EnumType.STRING)
  @Column (nullable = false)
  private LoanStatus status;

  @Column (name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at",nullable = false)
  private Instant updatedAt;

  @PrePersist 
  protected void onCreate() {
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
    if (this.status == null) {
      this.status = LoanStatus.PENDING;
    }
  }

  @PreUpdate 
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
