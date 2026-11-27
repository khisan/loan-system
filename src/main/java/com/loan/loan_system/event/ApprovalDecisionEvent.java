package com.loan.loan_system.event;

import com.loan.loan_system.entity.ApprovalRequest;

/** Fired after approve/reject/cancel is committed so the requester can be emailed. */
public record ApprovalDecisionEvent(
    ApprovalRequest request,
    String note,
    String decidedByEmail) {
}
