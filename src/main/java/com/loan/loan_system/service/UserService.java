package com.loan.loan_system.service;
import org.springframework.stereotype.Service;

import com.loan.loan_system.entity.User;
import com.loan.loan_system.repository.UserRepository;

@Service 
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email)
            .orElse(null);
  }
}
