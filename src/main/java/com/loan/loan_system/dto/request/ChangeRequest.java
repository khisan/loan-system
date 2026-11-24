package com.loan.loan_system.dto.request;

import java.util.Map;
import java.util.UUID;

import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.BusinessNumberType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Generic envelope used by resource controllers to stage a mutation. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRequest {

  @NotNull(message = "Entity type wajib diisi")
  private BusinessNumberType entityType;

  @NotNull(message = "Operasi wajib diisi")
  private ApprovalOperation operation;

  /** Required for UPDATE, DELETE and STATUS_CHANGE. */
  private UUID entityId;

  /** Proposed field values in the resource request DTO shape. */
  private Map<String, Object> payload;

  /** Needed by STATUS_CHANGE proposals. */
  private String targetStatus;

  private String reason;
}
