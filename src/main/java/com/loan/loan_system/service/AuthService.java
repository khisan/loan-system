package com.loan.loan_system.service;

import com.loan.loan_system.entity.User;
import com.loan.loan_system.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Tambahan BCrypt
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service // 1. Wajib tambahkan ini agar dibaca Spring
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    // 2. Gunakan Secret Key yang Tetap (Di real app ditaruh di application.properties)
    private final Key jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // Inisialisasi Encoder
    }

    // 3. Return String Token (Bukan cuma boolean)
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email);

        // Cek user ada DAN cocokkan password yang ter-hash
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            
            // Generate & Return JWT Token
            return Jwts.builder()
                    .setSubject(user.getEmail())
                    .claim("role", user.getRole())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Expired dalam 1 hari
                    .signWith(jwtSecretKey)
                    .compact();
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