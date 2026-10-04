package com.loan.loan_system.dto.request;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class RegisterRequest {
  private String name;
  private String email;
  private String password;
  private String role;
}
