package com.loan.loan_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loan.loan_system.entity.ApprovalRequest;
import com.loan.loan_system.enums.ApprovalStatus;
import com.loan.loan_system.enums.BusinessNumberType;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {

  List<ApprovalRequest> findAllByOrderByRequestedAtDesc();

  List<ApprovalRequest> findByStatusOrderByRequestedAtDesc(ApprovalStatus status);

  List<ApprovalRequest> findByEntityTypeOrderByRequestedAtDesc(BusinessNumberType entityType);

  List<ApprovalRequest> findByStatusAndEntityTypeOrderByRequestedAtDesc(
      ApprovalStatus status, BusinessNumberType entityType);
}
