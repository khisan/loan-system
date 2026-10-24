package com.loan.loan_system.config;

import com.loan.loan_system.entity.User;
import com.loan.loan_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
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
        // Cek dulu biar gak duplikat saat restart aplikasi
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .name("Super Admin")
                    .email("admin@loan.com")
                    .password(passwordEncoder.encode("admin123")) // Otomatis BCrypt
                    .role("admin")
                    .build();

            User staff = User.builder()
                    .name("Loan Staff")
                    .email("staff@loan.com")
                    .password(passwordEncoder.encode("staff123"))
                    .role("staff")
                    .build();

            userRepository.save(admin);
            userRepository.save(staff);
            log.info("✅ [DataSeeder] Initial users (Admin & Staff) seeded successfully.");
        }
    }
}