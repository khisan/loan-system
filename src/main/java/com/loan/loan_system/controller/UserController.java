package com.loan.loan_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan.loan_system.dto.response.UserResponse;
import com.loan.loan_system.service.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import com.loan.loan_system.dto.request.UserRequest;
import com.loan.loan_system.dto.response.ApiResponse;

@RestController
@RequestMapping ("/api/users")
public class UserController {
  @Autowired 
  private UserService userService;
  
  @GetMapping ("/email/{email}")
  public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable("email") String email) {
    UserResponse userResponse = userService.getUserByEmail(email);
    return ResponseEntity.ok(ApiResponse.success("User found", userResponse));
    }

  @PostMapping 
  public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserRequest userRequest) {
    UserResponse userResponse = userService.createUser(userRequest);
    return ResponseEntity.ok(ApiResponse.success("User created successfully", userResponse));
  }
}
