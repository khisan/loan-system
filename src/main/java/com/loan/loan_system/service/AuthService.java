package com.loan.loan_system.service;

import com.loan.loan_system.entity.User;
import com.loan.loan_system.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Tambahan BCrypt
import org.springframework.stereotype.Service;
import com.loan.loan_system.security.JwtUtil;

@Service // 1. Wajib tambahkan ini agar dibaca Spring
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder(); // Inisialisasi Encoder
    }

    // 3. Return String Token (Bukan cuma boolean)
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);

        // Cek user ada DAN cocokkan password yang ter-hash
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return jwtUtil.generateToken(email, user.getRole());
        }

        throw new RuntimeException("Email atau password salah!");
    }

    public boolean register(String email, String password) {
        // Cek dengan existsByEmail
        if (userRepository.existsByEmail(email)) {
            return false;
        }

        User user = new User();
        user.setEmail(email);
        // 4. Hash password sebelum disimpan ke Database!
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("user");

        userRepository.save(user);
        return true;
    }
}