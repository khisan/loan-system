package com.loan.loan_system.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.loan.loan_system.dto.request.LoginRequest;
import com.loan.loan_system.dto.request.RegisterRequest;
import com.loan.loan_system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.loan.loan_system.dto.response.ApiResponse;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  private AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest request) {
    if (authService.register(request.getEmail(), request.getPassword())) {
      return ResponseEntity.ok(ApiResponse.success("Register berhasil", "Register berhasil"));
    } else {
      return ResponseEntity.status(400).body(ApiResponse.failure("Register gagal"));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request) {
    String token = authService.login(request.getEmail(), request.getPassword());
    if (token != null) {
      return ResponseEntity.ok(ApiResponse.success("Login berhasil", token));
    } else {
      return ResponseEntity.status(400).body(ApiResponse.failure("Login gagal"));
    }
  }
}
