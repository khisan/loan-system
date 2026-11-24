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
  CUSTOMER("CUS", "customers", "customer_number"),
  COLLATERAL("COL", "collaterals", "collateral_number"),
  LOAN("LN", "loans", "loan_number"),
  GUARANTOR("GUA", "guarantors", "guarantor_number"),
  INVOICE("INV", "invoices", "invoice_number"),
  DISBURSEMENT("DISB", "disbursements", "disbursement_number");

  private final String prefix;
  private final String table;
  private final String column;
}
