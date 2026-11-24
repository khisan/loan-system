package com.loan.loan_system.service;

import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loan.loan_system.enums.BusinessNumberType;

import lombok.RequiredArgsConstructor;

/**
 * Generates per-year business numbers (e.g. LN-2026-0001, INV-2026-0001) from
 * a shared counter table. The row lock taken by the UPDATE makes allocation
 * safe under concurrent requests.
 */
@Service
@RequiredArgsConstructor
public class BusinessNumberService {
  private final JdbcTemplate jdbcTemplate;

  @Transactional
  public String next(BusinessNumberType type, Instant createdAt) {
    int year = createdAt.atZone(ZoneOffset.UTC).getYear();

    jdbcTemplate.update("""
        INSERT INTO business_number_sequence (number_type, year, next_value)
        VALUES (?, ?, 1)
        ON CONFLICT (number_type, year) DO NOTHING
        """, type.name(), year);

    Integer sequence = jdbcTemplate.queryForObject("""
        UPDATE business_number_sequence
        SET next_value = next_value + 1
        WHERE number_type = ? AND year = ?
        RETURNING next_value - 1
        """, Integer.class, type.name(), year);

    if (sequence == null) {
      throw new IllegalStateException(
          "Failed to allocate " + type.getPrefix() + " number for year " + year);
    }
    return String.format("%s-%d-%04d", type.getPrefix(), year, sequence);
  }
}
