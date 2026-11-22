package com.loan.loan_system.service;

import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Generates the human-facing loan number (LN-2026-0001) from a per-year
 * counter. The UUID remains the primary key and foreign key everywhere.
 */
@Service
@RequiredArgsConstructor
public class LoanNumberService {
  private final JdbcTemplate jdbcTemplate;

  @Transactional
  public String next(Instant createdAt) {
    int year = createdAt.atZone(ZoneOffset.UTC).getYear();

    jdbcTemplate.update("""
        INSERT INTO loan_number_sequence (year, next_value)
        VALUES (?, 1)
        ON CONFLICT (year) DO NOTHING
        """, year);

    Integer sequence = jdbcTemplate.queryForObject("""
        UPDATE loan_number_sequence
        SET next_value = next_value + 1
        WHERE year = ?
        RETURNING next_value - 1
        """, Integer.class, year);

    if (sequence == null) {
      throw new IllegalStateException("Failed to allocate loan number for year " + year);
    }
    return String.format("LN-%d-%04d", year, sequence);
  }
}
