package com.loan.loan_system.config;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.loan.loan_system.service.LoanNumberService;

import lombok.RequiredArgsConstructor;

/**
 * One-time-safe startup task: creates the per-year counter table, fills
 * loan_number for older rows, then locks in NOT NULL + uniqueness.
 *
 * ddl-auto=update adds the nullable column before this runner executes, so
 * existing databases can be migrated without a separate manual SQL step.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class LoanNumberBackfill implements ApplicationRunner {
  private final JdbcTemplate jdbcTemplate;
  private final LoanNumberService loanNumberService;

  private record BackfillRow(UUID id, Instant createdAt) {}

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    jdbcTemplate.execute("""
        CREATE TABLE IF NOT EXISTS loan_number_sequence (
          year INTEGER PRIMARY KEY,
          next_value INTEGER NOT NULL
        )
        """);

    // Rebuild counters from existing numbers so an interrupted backfill can
    // resume without reusing a number.
    jdbcTemplate.update("DELETE FROM loan_number_sequence");
    jdbcTemplate.update("""
        INSERT INTO loan_number_sequence (year, next_value)
        SELECT EXTRACT(YEAR FROM created_at AT TIME ZONE 'UTC')::int,
               COALESCE(MAX(SUBSTRING(loan_number FROM '(\\d+)$')::int), 0) + 1
        FROM loans
        GROUP BY 1
        """);

    List<BackfillRow> missing = jdbcTemplate.query("""
        SELECT id, created_at
        FROM loans
        WHERE loan_number IS NULL
        ORDER BY created_at, id
        """, (rs, rowNum) -> new BackfillRow(
            rs.getObject("id", UUID.class),
            rs.getTimestamp("created_at").toInstant()));

    for (BackfillRow row : missing) {
      String loanNumber = loanNumberService.next(row.createdAt());
      jdbcTemplate.update(
          "UPDATE loans SET loan_number = ? WHERE id = ?", loanNumber, row.id());
    }

    jdbcTemplate.execute(
        "CREATE UNIQUE INDEX IF NOT EXISTS ux_loans_loan_number ON loans (loan_number)");
    jdbcTemplate.execute("ALTER TABLE loans ALTER COLUMN loan_number SET NOT NULL");
  }
}
