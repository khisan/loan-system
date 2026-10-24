package com.loan.loan_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor 
public class User {
    @Id 
    @GeneratedValue (strategy = GenerationType. UUID)
    private UUID id;

    @Column (nullable = false)
    private String name;

    @Column (nullable = false, unique = true)
    private String email;

    @Column (nullable = false)
    private String password;

    @Column (nullable = false)
    private String role;

    @Column (nullable = false ,updatable = false)
    private Instant createdAt;

    @Column (nullable = false)
    private Instant updatedAt;

    @PrePersist 
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate 
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}