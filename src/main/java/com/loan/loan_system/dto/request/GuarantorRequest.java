package com.loan.loan_system.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuarantorRequest {
  @NotNull(message = "Loan ID wajib diisi")
  private UUID loanId;

  @NotBlank(message = "Nama penjamin wajib diisi")
  @Size(max = 255, message = "Nama penjamin maksimal 255 karakter")
  private String fullName;

  @NotBlank(message = "Nomor identitas wajib diisi")
  @Size(max = 255, message = "Nomor identitas maksimal 255 karakter")
  private String identityNumber;

  @NotBlank(message = "Email wajib diisi")
  @Email(message = "Format email tidak valid")
  @Size(max = 255, message = "Email maksimal 255 karakter")
  private String email;

  @NotBlank(message = "Nomor telepon wajib diisi")
  @Size(max = 255, message = "Nomor telepon maksimal 255 karakter")
  private String phoneNumber;

  @NotBlank(message = "Hubungan dengan peminjam wajib diisi")
  @Size(max = 255, message = "Hubungan maksimal 255 karakter")
  private String relationship;

  private String address;
}
