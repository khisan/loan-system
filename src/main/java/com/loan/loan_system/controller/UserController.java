package com.loan.loan_system.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.loan.loan_system.service.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import com.loan.loan_system.dto.UserResponse;

@RestController
@RequestMapping ("/api/users")
public class UserController {
  @Autowired 
  private UserService userService;
  
  @GetMapping ("/{email}")
  public ResponseEntity<?> getUserByEmail(@PathVariable("email") String email) {
    if (email == null || email.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "Email tidak boleh kosong"));
    }
    if (userService.getUserByEmail(email) != null) {
      UserResponse userResponse = new UserResponse(userService.getUserByEmail(email).getEmail(), userService.getUserByEmail(email).getRole());
      return ResponseEntity.ok(userResponse);
    } else {
      return ResponseEntity.status(404).body(Map.of("error", "User tidak ditemukan"));
    }
  }
}
