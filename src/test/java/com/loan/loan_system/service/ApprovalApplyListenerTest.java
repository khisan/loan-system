package com.loan.loan_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.loan.loan_system.dto.request.CustomerRequest;
import com.loan.loan_system.dto.response.CustomerResponse;
import com.loan.loan_system.entity.ApprovalRequest;
import com.loan.loan_system.enums.ApprovalOperation;
import com.loan.loan_system.enums.ApprovalStatus;
import com.loan.loan_system.enums.AuditAction;
import com.loan.loan_system.enums.BusinessNumberType;
import com.loan.loan_system.event.ApprovalApprovedEvent;
import com.loan.loan_system.security.Actor;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ApprovalApplyListenerTest {

  @Mock private ObjectMapper objectMapper;
  @Mock private AuditService auditService;
  @Mock private CustomerService customerService;
  @Mock private CollateralService collateralService;
  @Mock private LoanService loanService;
  @Mock private GuarantorService guarantorService;
  @Mock private InvoiceService invoiceService;
  @Mock private DisbursementService disbursementService;

  @InjectMocks private ApprovalApplyListener listener;

  @Test
  void approvedCreateUsesReservedNumberAndRecordsAudit() {
    Actor actor = new Actor(UUID.randomUUID(), "finance@loan.com", "finance");
    UUID createdId = UUID.randomUUID();
    ApprovalRequest request = ApprovalRequest.builder()
        .id(UUID.randomUUID())
        .entityType(BusinessNumberType.CUSTOMER)
        .operation(ApprovalOperation.CREATE)
        .businessNumber("CUS-2026-0001")
        .payloadJson("{\"customerNumber\":\"CUS-2026-0001\"}")
        .status(ApprovalStatus.APPROVED)
        .build();
    CustomerRequest dto = new CustomerRequest("Siti", "123", "siti@example.com", "0812", "Alamat");

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("customerNumber", "CUS-2026-0001");
    payload.put("fullName", "Siti");
    when(auditService.toMap(request.getPayloadJson())).thenReturn(payload);
    when(objectMapper.convertValue(any(Map.class), eq(CustomerRequest.class))).thenReturn(dto);
    when(customerService.applyCreate(eq(dto), eq("CUS-2026-0001"), eq(actor)))
        .thenReturn(CustomerResponse.builder().id(createdId).build());
    when(auditService.snapshot(BusinessNumberType.CUSTOMER, createdId))
        .thenReturn(Map.of("customer_number", "CUS-2026-0001"));
    when(auditService.toJson(any())).thenReturn("{}");

    listener.on(new ApprovalApprovedEvent(request, actor));

    verify(customerService).applyCreate(eq(dto), eq("CUS-2026-0001"), eq(actor));
    assertEquals(createdId, request.getEntityId());
    verify(auditService).record(
        eq(BusinessNumberType.CUSTOMER),
        eq(createdId),
        eq(AuditAction.CREATE),
        eq(actor),
        isNull(),
        eq("{}"),
        eq(request.getId()),
        anyString(),
        eq(false));
  }
}
