package com.loan.loan_system.config;

import com.loan.loan_system.entity.User;
import com.loan.loan_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.runners.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
    }

    private void seedUsers() {
        // Idempotent per email: existing databases also get the finance approver.
        seedIfMissing("admin@yopmail.com", "Super Admin", "admin123", "admin");
        seedIfMissing("staff@yopmail.com", "Loan Staff", "staff123", "staff");
        seedIfMissing("finance@yopmail.com", "Finance Approver", "finance123", "finance");
    }

    private void seedIfMissing(String email, String name, String rawPassword, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }
        userRepository.save(User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build());
        log.info("✅ [DataSeeder] Seeded {} user ({})", role, email);
    }
}