package com.loan.loan_system.config;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.service.BusinessNumberService;

import lombok.RequiredArgsConstructor;

/**
 * Startup migration for business numbers: creates the shared counter table,
 * rebuilds counters from existing numbers (so an interrupted backfill never
 * reuses one), fills older rows, then locks in uniqueness and NOT NULL.
 *
 * ddl-auto=update adds the nullable columns before this runner executes.
 */
@Component
@ConditionalOnProperty(name = "app.runners.enabled", havingValue = "true", matchIfMissing = true)
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class BusinessNumberBackfill implements ApplicationRunner {
  private final JdbcTemplate jdbcTemplate;
  private final BusinessNumberService businessNumberService;

  private record BackfillRow(UUID id, Instant createdAt) {}

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    jdbcTemplate.execute("""
        CREATE TABLE IF NOT EXISTS business_number_sequence (
          number_type VARCHAR(32) NOT NULL,
          year INTEGER NOT NULL,
          next_value INTEGER NOT NULL,
          PRIMARY KEY (number_type, year)
        )
        """);

    for (BusinessNumberType type : BusinessNumberType.values()) {
      rebuildCounters(type);
      backfillMissing(type);

      jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_" + type.getTable() + "_" + type.getColumn()
          + " ON " + type.getTable() + " (" + type.getColumn() + ")");
      jdbcTemplate.execute(
          "ALTER TABLE " + type.getTable() + " ALTER COLUMN " + type.getColumn() + " SET NOT NULL");
    }

    // Superseded by business_number_sequence.
    jdbcTemplate.execute("DROP TABLE IF EXISTS loan_number_sequence");
  }

  /** Derives each year's next value from the numbers already stored in the table. */
  private void rebuildCounters(BusinessNumberType type) {
    jdbcTemplate.update(
        "DELETE FROM business_number_sequence WHERE number_type = ?", type.name());

    String column = type.getColumn();
    String sql = """
        INSERT INTO business_number_sequence (number_type, year, next_value)
        SELECT ?,
               COALESCE(
                 SUBSTRING(%1$s FROM '-(\\d{4})-')::int,
                 SUBSTRING(%1$s FROM '-(\\d{4})\\d{2}-')::int,
                 EXTRACT(YEAR FROM created_at AT TIME ZONE 'UTC')::int),
               COALESCE(MAX(SUBSTRING(%1$s FROM '(\\d+)$')::int), 0) + 1
        FROM %2$s
        GROUP BY 2
        """.formatted(column, type.getTable());
    jdbcTemplate.update(sql, type.name());
  }

  private void backfillMissing(BusinessNumberType type) {
    String sql = """
        SELECT id, created_at
        FROM %s
        WHERE %s IS NULL
        ORDER BY created_at, id
        """.formatted(type.getTable(), type.getColumn());
    List<BackfillRow> missing = jdbcTemplate.query(sql, (rs, rowNum) -> new BackfillRow(
            rs.getObject("id", UUID.class),
            rs.getTimestamp("created_at").toInstant()));

    for (BackfillRow row : missing) {
      String number = businessNumberService.next(type, row.createdAt());
      jdbcTemplate.update(
          "UPDATE " + type.getTable() + " SET " + type.getColumn() + " = ? WHERE id = ?",
          number, row.id());
    }
  }
}
