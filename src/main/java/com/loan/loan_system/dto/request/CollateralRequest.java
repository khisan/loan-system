package com.loan.loan_system.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.loan.loan_system.enums.CollateralType;
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
public class CollateralRequest {
  @NotNull(message = "Loan ID wajib diisi")
  private UUID loanId;

  @NotNull(message = "Jenis agunan wajib diisi")
  private CollateralType type;

  @NotBlank(message = "Nomor dokumen agunan wajib diisi")
  @Size(max = 255, message = "Nomor dokumen maksimal 255 karakter")
  private String documentNumber;

  @NotNull(message = "Nilai pasar agunan wajib diisi")
  @Positive(message = "Nilai pasar agunan harus lebih besar dari 0")
  @Digits(integer = 13, fraction = 2, message = "Format nilai pasar agunan tidak valid")
  private BigDecimal marketValue;

  @NotNull(message = "Nilai ikat jaminan wajib diisi")
  @Positive(message = "Nilai ikat jaminan harus lebih besar dari 0")
  @Digits(integer = 13, fraction = 2, message = "Format nilai ikat jaminan tidak valid")
  private BigDecimal pledgedValue;

  private String description;
}
