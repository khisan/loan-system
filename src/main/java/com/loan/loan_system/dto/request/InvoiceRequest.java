package com.loan.loan_system.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.loan.loan_system.enums.InvoiceStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {
  @NotNull(message = "Loan ID wajib diisi")
  private UUID loanId;

  @NotBlank(message = "Nomor invoice wajib diisi")
  @Size(max = 255, message = "Nomor invoice maksimal 255 karakter")
  private String invoiceNumber;

  @NotNull(message = "Nomor angsuran wajib diisi")
  @Positive(message = "Nomor angsuran harus lebih besar dari 0")
  private Integer installmentNumber;

  @NotNull(message = "Nominal pokok wajib diisi")
  @PositiveOrZero(message = "Nominal pokok tidak boleh negatif")
  @Digits(integer = 13, fraction = 2, message = "Format nominal pokok tidak valid")
  private BigDecimal principalAmount;

  @NotNull(message = "Nominal bunga wajib diisi")
  @PositiveOrZero(message = "Nominal bunga tidak boleh negatif")
  @Digits(integer = 13, fraction = 2, message = "Format nominal bunga tidak valid")
  private BigDecimal interestAmount;

  @PositiveOrZero(message = "Nominal denda tidak boleh negatif")
  @Digits(integer = 13, fraction = 2, message = "Format nominal denda tidak valid")
  private BigDecimal penaltyAmount;

  @NotNull(message = "Tanggal jatuh tempo wajib diisi")
  private Instant dueDate;

  private Instant paidAt;
  private InvoiceStatus status;
}
