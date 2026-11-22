package com.loan.loan_system.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final PasswordEncoder passwordEncoder;

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

  public UserResponse createUser(UserRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email sudah terdaftar");
    }
    User user = User.builder()
        .name(request.getName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(request.getRole())
        .build();
    return toResponse(userRepository.save(user));
  }

  /** PUT /api/users/{id}. Password kosong = kunci lama tetap dipakai. */
  public UserResponse updateUser(UUID id, UserRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "User tidak ditemukan dengan id: " + id));

    if (!user.getEmail().equalsIgnoreCase(request.getEmail()) &&
        userRepository.existsByEmail(request.getEmail())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email sudah terdaftar");
    }

    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setRole(request.getRole());
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }
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
