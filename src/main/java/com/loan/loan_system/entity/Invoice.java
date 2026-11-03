package com.loan.loan_system.entity;

import com.loan.loan_system.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber; // Ref No: INV-202610-001

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber; // Angsuran ke-N (misal: 1, 2, 3...)

    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount; // Komponen Pokok

    @Column(name = "interest_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal interestAmount; // Komponen Bunga

    @Column(name = "penalty_amount", precision = 15, scale = 2)
    private BigDecimal penaltyAmount; // Denda Keterlambatan

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount; // Pokok + Bunga + Denda

    @Column(name = "due_date", nullable = false)
    private Instant dueDate;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.status == null) {
            this.status = InvoiceStatus.UNPAID;
        }
        if (this.penaltyAmount == null) {
            this.penaltyAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}