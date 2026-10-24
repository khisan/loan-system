package com.loan.loan_system.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table (name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor 
public class Customer {
  
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column (nullable = false)
  private String fullName;

  @Column (nullable = false, unique = true, length = 16)
  private String identityNumber;

  @Column (nullable = false, unique = true)
  private String email;

  @Column (nullable = false)
  private String phoneNumber;

  @Column (columnDefinition = "TEXT")
  private String address;

  @Column (nullable = false,updatable = false)
  @CreationTimestamp
  private Instant createdAt;

  @Column (nullable = false)
  @UpdateTimestamp
  private Instant updatedAt;
}
