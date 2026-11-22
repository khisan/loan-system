package com.loan.loan_system.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loan.loan_system.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /** Email is unique, so one row at most — AuthService and UserService rely on Optional. */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
