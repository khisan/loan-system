package com.loan.loan_system.event;

import com.loan.loan_system.entity.ApprovalRequest;
import com.loan.loan_system.security.Actor;

/**
 * Fired synchronously inside the approval transaction. The listener applies the
 * staged payload; keeping this as an event avoids a service dependency cycle.
 */
public record ApprovalApprovedEvent(ApprovalRequest request, Actor actor) {
}
