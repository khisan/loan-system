package com.loan.loan_system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Human-facing business numbers. UUIDs remain the primary/foreign keys; these
 * values are only stable, readable references such as LN-2026-0001.
 */
@Getter
@RequiredArgsConstructor
public enum BusinessNumberType {
  CUSTOMER("CUS", "customers", "customer_number", "customerNumber"),
  COLLATERAL("COL", "collaterals", "collateral_number", "collateralNumber"),
  LOAN("LN", "loans", "loan_number", "loanNumber"),
  GUARANTOR("GUA", "guarantors", "guarantor_number", "guarantorNumber"),
  INVOICE("INV", "invoices", "invoice_number", "invoiceNumber"),
  DISBURSEMENT("DISB", "disbursements", "disbursement_number", "disbursementNumber");

  private final String prefix;
  private final String table;
  private final String column;
  /** DTO/JSON key of the business number, used when staging CREATE proposals. */
  private final String field;
}
