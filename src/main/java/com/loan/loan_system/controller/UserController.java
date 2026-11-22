package com.loan.loan_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan.loan_system.dto.request.UserRequest;
import com.loan.loan_system.dto.response.ApiResponse;
import com.loan.loan_system.dto.response.UserResponse;
import com.loan.loan_system.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
    return ResponseEntity.ok(
        ApiResponse.success("Users retrieved successfully", userService.getUsers()));
  }

  @GetMapping("/email/{email}")
  public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
    return ResponseEntity.ok(ApiResponse.success("User found", userService.getUserByEmail(email)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserRequest userRequest) {
    return ResponseEntity.ok(
        ApiResponse.success("User created successfully", userService.createUser(userRequest)));
  }
}
