package com.loan.loan_system.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class UserRequest {
  private String name;
  private String email;
  private String password;
  private String role;
}
