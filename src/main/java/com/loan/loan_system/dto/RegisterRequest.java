package com.loan.loan_system.dto;

import org.hibernate.validator.constraints.Normalized;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class RegisterRequest {
  private String email;
  private String password;
  private String role;
}
