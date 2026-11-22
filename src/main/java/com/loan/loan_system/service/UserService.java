package com.loan.loan_system.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.loan.loan_system.dto.request.UserRequest;
import com.loan.loan_system.dto.response.UserResponse;
import com.loan.loan_system.entity.User;
import com.loan.loan_system.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<UserResponse> getUsers() {
    return userRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public UserResponse getUserByEmail(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "User tidak ditemukan dengan email: " + email));
    return toResponse(user);
  }

  public UserResponse createUser(UserRequest userRequest) {
    if (userRepository.existsByEmail(userRequest.getEmail())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email sudah terdaftar");
    }
    User user = User.builder()
        .name(userRequest.getName())
        .email(userRequest.getEmail())
        .password(userRequest.getPassword())
        .role(userRequest.getRole())
        .build();
    return toResponse(userRepository.save(user));
  }

  private UserResponse toResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .name(user.getName())
        .email(user.getEmail())
        .role(user.getRole())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
