package com.loan.loan_system.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementRequest {
  @NotNull(message = "Loan ID wajib diisi")
  private UUID loanId;

  @NotBlank(message = "Nomor pencairan wajib diisi")
  @Size(max = 255, message = "Nomor pencairan maksimal 255 karakter")
  private String disbursementNumber;

  @NotNull(message = "Nominal pencairan wajib diisi")
  @Positive(message = "Nominal pencairan harus lebih besar dari 0")
  @Digits(integer = 13, fraction = 2, message = "Format nominal pencairan tidak valid")
  private BigDecimal amount;

  @NotBlank(message = "Nama bank wajib diisi")
  @Size(max = 255, message = "Nama bank maksimal 255 karakter")
  private String bankName;

  @NotBlank(message = "Nomor rekening wajib diisi")
  @Size(max = 255, message = "Nomor rekening maksimal 255 karakter")
  private String bankAccountNumber;

  @NotBlank(message = "Nama pemilik rekening wajib diisi")
  @Size(max = 255, message = "Nama pemilik rekening maksimal 255 karakter")
  private String bankAccountName;
}
