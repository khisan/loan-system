package com.loan.loan_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import com.loan.loan_system.enums.BusinessNumberType;

@ExtendWith(MockitoExtension.class)
class BusinessNumberServiceTest {
  @Mock
  private JdbcTemplate jdbcTemplate;

  @InjectMocks
  private BusinessNumberService businessNumberService;

  @Test
  void generatesPrefixedNumberPerType() {
    Instant createdAt = Instant.parse("2026-05-01T00:00:00Z");
    String[][] expected = {
        {"CUSTOMER", "CUS-2026-0001"},
        {"COLLATERAL", "COL-2026-0001"},
        {"LOAN", "LN-2026-0001"},
        {"GUARANTOR", "GUA-2026-0001"},
        {"INVOICE", "INV-2026-0001"},
        {"DISBURSEMENT", "DISB-2026-0001"},
    };

    for (String[] pair : expected) {
      when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(pair[0]), eq(2026)))
          .thenReturn(1);
      assertEquals(pair[1], businessNumberService.next(BusinessNumberType.valueOf(pair[0]), createdAt));
    }
  }

  @Test
  void counterResetsPerYear() {
    when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("LOAN"), eq(2027)))
        .thenReturn(12);

    assertEquals(
        "LN-2027-0012",
        businessNumberService.next(BusinessNumberType.LOAN, Instant.parse("2027-01-01T00:00:00Z")));
  }

  @Test
  void failsWhenSequenceIsMissing() {
    when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("INVOICE"), eq(2026)))
        .thenReturn(null);

    assertThrows(
        IllegalStateException.class,
        () -> businessNumberService.next(BusinessNumberType.INVOICE, Instant.parse("2026-01-01T00:00:00Z")));
  }
}
