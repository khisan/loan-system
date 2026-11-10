package com.loan.loan_system.entity;

import com.loan.loan_system.enums.CollateralType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "collaterals")
@SQLDelete(sql = "UPDATE collaterals SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collateral {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Enumerated(EnumType.STRING)
    @Column(name = "collateral_type", nullable = false)
    private CollateralType type;

    @Column(name = "document_number", nullable = false)
    private String documentNumber; // No. SHM / BPKB / Sertifikat Deposito

    @Column(name = "market_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal marketValue; // Nilai Pasar

    @Column(name = "pledged_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal pledgedValue; // Nilai Ikat Jaminan (LTV ratio calculation)

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}