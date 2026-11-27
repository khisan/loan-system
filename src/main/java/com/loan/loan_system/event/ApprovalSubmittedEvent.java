package com.loan.loan_system.event;

import com.loan.loan_system.entity.ApprovalRequest;

/** Fired after a proposal is committed so approvers can be emailed. */
public record ApprovalSubmittedEvent(ApprovalRequest request) {
}
