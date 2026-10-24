package com.loan.loan_system.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class CustomerRequest {
  private String fullName;
  private String identityNumber;
  private String email;
  private String phoneNumber;
  private String address;
}
