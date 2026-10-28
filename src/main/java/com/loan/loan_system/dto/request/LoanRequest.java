package com.loan.loan_system.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class LoanRequest {
  @NotNull (message = "Customer ID wajib diisi")
  private UUID customerId;

  @NotNull(message = "Nominal pinjaman wajib diisi")
  @DecimalMin(value = "100000.00", message = "Minimal pinjaman adalah Rp 100.000")
  @Digits (integer = 13,fraction = 2,message = "Format nominal tidak valid")
  private BigDecimal amount;

  @NotNull(message = "Bunga pinjaman wajib diisi")
  @Positive (message = "Bunga harus lebih besar dari 0")
  private Double interestRate;

  @NotNull(message = "Tenor pinjaman wajib diisi")
  @Min (value = 1, message = "Tenor minimal 1 bulan")
@Max (value = 120, message = "Tenor maksimal 120 bulan (10 tahun)")
  private String tenorMonths;
}
