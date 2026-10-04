package com.loan.loan_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan.loan_system.dto.response.UserResponse;
import com.loan.loan_system.service.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import com.loan.loan_system.dto.response.ApiResponse;

@RestController
@RequestMapping ("/api/users")
public class UserController {
  @Autowired 
  private UserService userService;
  
  @GetMapping ("/{email}")
  public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable("email") String email) {
    if (email == null || email.isEmpty()) {
      return ResponseEntity.badRequest().body(ApiResponse.failure("Email tidak boleh kosong"));
    }
    if (userService.getUserByEmail(email) != null) {
      UserResponse userResponse = new UserResponse(userService.getUserByEmail(email).getName(), userService.getUserByEmail(email).getEmail(), userService.getUserByEmail(email).getRole());
      return ResponseEntity.ok(ApiResponse.success("User ditemukan", userResponse));
    } else {
      return ResponseEntity.status(404).body(ApiResponse.failure("User tidak ditemukan"));
    }
  }
}
