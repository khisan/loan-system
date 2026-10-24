package com.loan.loan_system.service;
import org.springframework.stereotype.Service;

import com.loan.loan_system.dto.response.UserResponse;
import com.loan.loan_system.dto.request.UserRequest;
import com.loan.loan_system.entity.User;
import com.loan.loan_system.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class UserService {
  private final UserRepository userRepository;

  public UserResponse getUserByEmail(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
  }

  public UserResponse createUser(UserRequest userRequest) {
    User user = User.builder()
            .name(userRequest.getName())
            .email(userRequest.getEmail())
            .password(userRequest.getPassword())
            .role(userRequest.getRole())
            .build();
    User savedUser = userRepository.save(user);
    return UserResponse.builder()
            .id(savedUser.getId())
            .name(savedUser.getName())
            .email(savedUser.getEmail())
            .role(savedUser.getRole())
            .createdAt(savedUser.getCreatedAt())
            .updatedAt(savedUser.getUpdatedAt())
            .build();
  }
}
