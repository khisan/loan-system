package com.loan.loan_system.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.loan.loan_system.dto.LoginRequest;
import com.loan.loan_system.dto.RegisterRequest;
import com.loan.loan_system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  private AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
    if (authService.register(request.getEmail(), request.getPassword())) {
      return ResponseEntity.ok("Register berhasil");
    } else {
      return ResponseEntity.status(400).body("Register gagal");
    }
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody LoginRequest request) {
    String token = authService.login(request.getEmail(), request.getPassword());
    if (token != null) {
      return ResponseEntity.ok(token);
    } else {
      return ResponseEntity.status(400).body("Login gagal");
    }
  }
}
