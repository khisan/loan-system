package com.loan.loan_system.enums;

/** Append-only audit trail actions. */
public enum AuditAction {
  SUBMIT,
  CREATE,
  UPDATE,
  DELETE,
  STATUS_CHANGE,
  APPROVE,
  REJECT,
  CANCEL
}
